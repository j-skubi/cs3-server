import { useSelector } from "react-redux";
import { selectLokComponents } from "../Redux/dataSelectors";
import { lokComponent, lokFunction } from "../definitions/types";
import './LokView.css'
import { useState } from "react";
import store from "../Redux/store";

const Lok = (props: { component: lokComponent, key: number, index: number }) => {
    const [speedGoal, setSpeedGoal] = useState(0);
    const [activeFunctions, setActiveFunctions] = useState<{ [key: number]: boolean }>({});

    const onSpeedGoalChange = (goal: number, viewID: number) => {
        setSpeedGoal(goal);
        store.dispatch({ type: 'setTrainSpeed', payload: { viewType: "COMPONENT-VIEW", viewID: viewID, speed: goal } });
    };

    const handleFunctionClick = (lokFunction: lokFunction) => {
        const isActive = !!activeFunctions[lokFunction.index];
        if (lokFunction.isToggle) {
            setActiveFunctions({
                ...activeFunctions,
                [lokFunction.index]: !isActive,
            });
        } else {
            setActiveFunctions({
                ...activeFunctions,
                [lokFunction.index]: true,
            });
            setTimeout(() => {
                setActiveFunctions({
                    ...activeFunctions,
                    [lokFunction.index]: false,
                });
            }, 600); // Highlight for a short period
        }

        store.dispatch({
            type: 'toggleLokFunction',
            payload: {
                viewType: "COMPONENT-VIEW",
                viewID: props.component.viewID,
                index: lokFunction.index,
            }
        });
    };

    return (
        <div key={props.index} className="lok-container">
            <h2 className="lok-name">{props.component.name}</h2>
            <div className="lok-controls">
                <div className="lok-direction" onClick={() => store.dispatch({ type: 'changeComponentState', payload: { viewType: "COMPONENT-VIEW", viewID: props.component.viewID } })}>
                    Direction: {props.component.direction}
                </div>
                <div className="lok-speed">
                    <label htmlFor={`speed-${props.component.viewID}`}>Speed: {props.component.speed}</label>
                    <input
                        type="range"
                        id={`speed-${props.component.viewID}`}
                        min="0"
                        max="1000"
                        value={speedGoal}
                        onChange={(e) => onSpeedGoalChange(parseInt(e.target.value), props.component.viewID)}
                    />
                </div>
                <div className="lok-functions">
                    {props.component.lokFunctions
                        .sort((a, b) => a.index - b.index)
                        .map(lokFunction => (
                            <button
                                key={lokFunction.index}
                                className={`lok-function-button ${activeFunctions[lokFunction.index] ? 'active' : ''}`}
                                onClick={() => handleFunctionClick(lokFunction)}
                            >
                                {lokFunction.name}
                            </button>
                        ))}
                </div>
            </div>
        </div>
    );
};

const LokList = () => {
    const lokComponents = useSelector(selectLokComponents);

    return (
        <div className="lok-list">
            {lokComponents.map((component, index) => (
                <Lok component={component} key={index} index={index}></Lok>
            ))}
        </div>
    );
};


export default LokList;
