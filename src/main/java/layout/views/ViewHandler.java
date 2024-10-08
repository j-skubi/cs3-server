package layout.views;

import com.google.gson.JsonObject;
import exceptions.CorruptedSaveFile;
import layout.model.LayoutComponent;
import layout.views.componentView.ComponentView;
import utils.IDGenerator;
import utils.Utils;
import utils.datastructures.AVLTree;
import utils.datastructures.Command;
import utils.datastructures.PriorityBlockingQueueWrapper;

import java.util.HashMap;

public class ViewHandler {
    private final HashMap<String,View> views;

    public ViewHandler(JsonObject json, AVLTree<LayoutComponent> model) throws CorruptedSaveFile {
        views = new HashMap<>();
        views.put(ComponentView.TYPE, new ComponentView(json.get(ComponentView.TYPE).getAsJsonObject(),model));
    }

    public JsonObject save() {
        JsonObject json = new JsonObject();
        views.forEach((viewType, view) -> json.add(viewType, view.save()));
        return json;
    }

    public RequestViewClass requestViewClass(JsonObject command, PriorityBlockingQueueWrapper<Command> queue, int clientId) {
        return new RequestViewClass(command,queue, clientId);
    }
    public class RequestViewClass implements Runnable {
        private final PriorityBlockingQueueWrapper<Command> queue;
        private final JsonObject command;
        private final int clientId;
        public RequestViewClass(JsonObject command, PriorityBlockingQueueWrapper<Command> queue, int clientId) {
            this.queue = queue;
            this.command = command;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            System.out.format(Utils.getFormatString(), "[" + Thread.currentThread().getName() + "]", "[" + this.getClass().getSimpleName() + "]", "Working on: RequestView from Client " + clientId);

            String viewType = command.get("viewType").getAsString();

            JsonObject header = new JsonObject();
            header.addProperty("from", "view");
            header.addProperty("to", clientId);
            header.addProperty("commandType", "requestViewAnswer");

            JsonObject body = views.get(viewType).toClient();

            JsonObject response = new JsonObject();
            response.add("header", header);
            response.add("body", body);

            queue.add(new Command(1000, response));
        }
    }
    public ChangeComponentStateClass changeComponentStateClass(JsonObject command, PriorityBlockingQueueWrapper<Command> queue) {
        return new ChangeComponentStateClass(command,queue);
    }
    public class ChangeComponentStateClass implements Runnable {
        private final PriorityBlockingQueueWrapper<Command> queue;
        private final JsonObject command;

        public ChangeComponentStateClass(JsonObject command, PriorityBlockingQueueWrapper<Command> queue) {
            this.queue = queue;
            this.command = command;
        }

        @Override
        public void run() {
            System.out.format(Utils.getFormatString(), "[" + Thread.currentThread().getName() + "]", "[" + this.getClass().getSimpleName() + "]", "Working on: ChangeState");

            String viewType = command.get("viewType").getAsString();

            JsonObject header = new JsonObject();
            header.addProperty("from", "view");
            header.addProperty("to", "cs3");
            header.addProperty("commandType", "setState");

            JsonObject body = views.get(viewType).changeState(command.get("viewID").getAsInt());

            JsonObject response = new JsonObject();
            response.add("header", header);
            response.add("body", body);

            queue.add(new Command(200,response));
        }
    }
    public AddViewComponentClass addViewComponentClass(JsonObject command, PriorityBlockingQueueWrapper<Command> queue, AVLTree<LayoutComponent> model, IDGenerator generator) {
        return new AddViewComponentClass(command, queue, model, generator);
    }
    public class AddViewComponentClass implements Runnable {
        private final PriorityBlockingQueueWrapper<Command> queue;
        private final JsonObject command;
        private final AVLTree<LayoutComponent> model;
        private final IDGenerator generator;
        public AddViewComponentClass(JsonObject json, PriorityBlockingQueueWrapper<Command> queue, AVLTree<LayoutComponent> model, IDGenerator generator) {
            this.command = json;
            this.queue = queue;
            this.model = model;
            this.generator = generator;
        }

        @Override
        public void run() {
            System.out.format(Utils.getFormatString(), "[" + Thread.currentThread().getName() + "]", "[" + this.getClass().getSimpleName() + "]", "Working on: " + command.toString());
            try {
                JsonObject newComponent = views.get(command.get("viewType").getAsString()).addViewComponent(command.get("component").getAsJsonObject(), model, generator);

                JsonObject json = new JsonObject();
                JsonObject header = new JsonObject();
                header.addProperty("commandType", "addViewComponent");
                header.addProperty("from", "view");
                header.addProperty("to", "broadcast");

                json.add("header",header);
                json.add("body", newComponent);

                queue.add(new Command(1000, json));
            } catch (CorruptedSaveFile e) {
                throw new RuntimeException(e);
            }
        }
    }
    public SetTrainSpeedClass setTrainSpeedClass(JsonObject command, PriorityBlockingQueueWrapper<Command> queue) {
        return new SetTrainSpeedClass(command, queue);
    }
    public class SetTrainSpeedClass implements Runnable{
        private final JsonObject command;
        private final PriorityBlockingQueueWrapper<Command> queue;
        public SetTrainSpeedClass(JsonObject json, PriorityBlockingQueueWrapper<Command> queue) {
            this.command = json;
            this.queue = queue;
        }

        @Override
        public void run() {
            System.out.format(Utils.getFormatString(), "[" + Thread.currentThread().getName() + "]", "[" + this.getClass().getSimpleName() + "]", "Working on: SetTrainSpeed");

            if (command.get("speed").getAsInt() > 1000) {
                System.err.format(Utils.getFormatString(), "[" + Thread.currentThread().getName() + "]", "[" + this.getClass().getSimpleName() + "]", "Illegal Speed Value: " + command.get("speed").getAsInt());
                return;
            }

            String viewType = command.get("viewType").getAsString();

            JsonObject header = new JsonObject();
            header.addProperty("from", "view");
            header.addProperty("to", "cs3");
            header.addProperty("commandType", "setLokSpeed");

            JsonObject body = views.get(viewType).setTrainSpeed(command.get("viewID").getAsInt(), command.get("speed").getAsInt());

            JsonObject response = new JsonObject();
            response.add("header", header);
            response.add("body", body);

            queue.add(new Command(200,response));
        }
    }
    public ActivateLokFunctionClass activateLokFunctionClass(JsonObject command, PriorityBlockingQueueWrapper<Command> queue) {
        return new ActivateLokFunctionClass(command,queue);
    }
    public class ActivateLokFunctionClass implements Runnable{
        private final JsonObject command;
        private final PriorityBlockingQueueWrapper<Command> queue;
        public ActivateLokFunctionClass(JsonObject json, PriorityBlockingQueueWrapper<Command> queue) {
            this.command = json;
            this.queue = queue;
        }
        @Override
        public void run() {
            System.out.format(Utils.getFormatString(), "[" + Thread.currentThread().getName() + "]", "[" + this.getClass().getSimpleName() + "]", "Activate Function");

            String viewType = command.get("viewType").getAsString();

            JsonObject header = new JsonObject();
            header.addProperty("from", "view");
            header.addProperty("to", "cs3");
            header.addProperty("commandType", "activateLokFunction");

            JsonObject body = views.get(viewType).activateLokFunction(command.get("viewID").getAsInt(), command.get("index").getAsInt());

            JsonObject response = new JsonObject();
            response.add("header", header);
            response.add("body", body);

            queue.add(new Command(200,response));
        }
    }
}
