


export type state = {
    data: dataState,
    control: controlState
}

export type controlState = {
    connectionStatus: connectionStatus,
    clientID: number,
    viewType: string | undefined,
    editMode: boolean
}

export type connectionStatus = "loading" | "connected"

export type dataState = {
    visibleView: undefined | string
    viewData: {
        turnoutData: turnoutComponent[]
        lokData: lokComponent[]
        sensorData: sensorComponent[]
    }
}

export type viewComponent = turnoutComponent | lokComponent | sensorComponent

export type sensorComponent = {
    type: "SENSOR",
    viewID: number,
    name: string,
    isOccupied: boolean
}

export type lokComponent = {
    type: "LOK"
    viewID: number,
    name: string,
    direction: string,
    speed: number,
    lokFunctions: lokFunction[]
}
export type lokFunction = {
    index: number,
    isToggle: boolean,
    hasInputField: boolean,
    isActive: boolean,
    name: string,    
}
export type basicViewComponent = {
    viewID: number,
    name: string,
    state: string,
    legalStates: string[],
    addressSpaceMappings: addressSpaceMapping[] | undefined
}

export type turnoutComponent = basicViewComponent & {
    type: "TURNOUT",
}

export type addressSpaceMapping = {
    addressSpace: string,
    stateMappings: {
        state: string,
        mapping: {
            address: number,
            mapping: number
        }[]
    }[]
}