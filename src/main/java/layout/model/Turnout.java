package layout.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import exceptions.IllegalStateException;
import utils.Utils;
import utils.datastructures.Command;
import utils.datastructures.Event;
import utils.datastructures.PriorityBlockingQueueWrapper;

import java.util.ArrayList;
import java.util.List;

public class Turnout extends LayoutComponent  {
    private final List<String> legalStates;
    private String state = "Straight";
    public Turnout(int id) {
        super(id, "TURNOUT");
        this.legalStates = new ArrayList<>();
    }
    public Turnout(JsonObject json) {
        super(json);
        legalStates = new ArrayList<>();
        json.get("legalStates").getAsJsonArray().forEach(state -> legalStates.add(state.getAsString()));
    }
    @Override
    public JsonObject save() {
        JsonObject json = super.save();

        JsonArray legalStatesArray = new JsonArray();
        legalStates.forEach(legalStatesArray::add);
        json.add("legalStates", legalStatesArray);

        return json;
    }
    public String getState() {
        return state;
    }
    public JsonArray getLegalStatesAsJsonArray() {
        JsonArray json = new JsonArray();
        legalStates.forEach(json::add);
        return json;
    }
    public List<String> getLegalStates() {
        return legalStates;
    }
    public JsonObject setState(String newState) throws IllegalStateException {
        JsonObject json = new JsonObject();

        if (!legalStates.contains(newState)) {
            throw new IllegalStateException("State " + newState + " is not included in legalStates");
        }
        json.addProperty("type", this.type);
        json.addProperty("modelID", this.id);
        json.addProperty("newState", newState);
        json.add("addressSpaceMappings", addressSpaceMappings);

        return json;
    }
    @Override
    public boolean hasAddress(JsonObject command, int address) {
        if (!command.get("type").getAsString().equals("MA")) {
            return false;
        }
        return addressSpaceMappings.get(command.get("addressSpace").getAsString()).getAsJsonObject().entrySet().stream()
                .flatMap(stateMap -> stateMap.getValue().getAsJsonObject().keySet().stream())
                .anyMatch(key -> Integer.parseInt(key) == address);
    }
    @Override
    synchronized public void applyStandaloneMessage(JsonObject json, PriorityBlockingQueueWrapper<Command> queue) {
        if (json.get("power").getAsInt() == 0) {
            return;
        }
        JsonObject currentState = addressSpaceMappings.get(json.get("addressSpace").getAsString()).getAsJsonObject().get(state).getAsJsonObject().deepCopy();
        currentState.remove(json.get("address").getAsString());
        currentState.addProperty(json.get("address").getAsString(), json.get("state").getAsInt());
        addressSpaceMappings.get(json.get("addressSpace").getAsString()).getAsJsonObject().entrySet().forEach(entry -> {    //entry = (state: string -> addressMapping: jsonObject)
            if (entry.getValue().getAsJsonObject().entrySet().stream().allMatch(addressStatePair ->                         //addressStatePair = (address: int -> state: int)
                currentState.get(addressStatePair.getKey()).getAsInt() == addressStatePair.getValue().getAsInt()
            )) {
                JsonObject notify = new JsonObject();
                notify.addProperty("type", type);
                notify.addProperty("newState", entry.getKey());
                notifyChange(notify, queue);
            }
        });
    }
    @Override
    synchronized public void notifyChange(JsonObject json, PriorityBlockingQueueWrapper<Command> queue) {
        if (!json.get("type").getAsString().equals(type)) {
            System.err.println("Wrong TYPE");
            return;
        }
        if (state.equals(json.get("newState").getAsString())) {
            return;
        }
        if (legalStates.contains(json.get("newState").getAsString())) {
            System.err.println(json.get("newState").getAsString());
            JsonObject additionalInfo = new JsonObject();
            additionalInfo.addProperty("oldState", state);
            state = json.get("newState").getAsString();
            additionalInfo.addProperty("newState", state);
            this.notifyListeners(new Event(Event.EventType.StateChange, additionalInfo, queue, this));
            System.out.format(Utils.getFormatString(), "[" + Thread.currentThread().getName() + "]", "[" + this.getClass().getSimpleName() + "]", "Notified All Listeners");
        }


    }

}
