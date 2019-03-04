type msg =
  | Sent(string)
  | Received(string);

type action =
  | DisplayMessage(msg)
  | SendMessage(string);

type state = {
  websocket: Websocket.t(Js.t({.., _type: string})),
  history: list(msg),
  textBox: string
};

let component = ReasonReact.reducerComponent("ChatBox");

let make(~ws, ~you: string, _children) {
  ...component,
  initialState: () => {
    websocket: ws,
    history: [],
    textBox: ""
  },
  reducer: (action, state) => switch (action) {

  },
  didMount: self => {
    Websocket.onMessage(self.state.websocket, msg => switch(msg##_type) {
      | "actors.Ping" => self.send(DisplayMessage)
    });
  }
  willUnmount: self => {
    Websocket.close(self.state.websocket)
  },
  render: self =>
    <div>
      <h3> you </h3>
      <div>
        (...List.map(renderHistory, self.state.msgs))
      </div>
      <input type_="text" placeholder="Send a message" value=self.state.textBox onChange=onTextBoxChange />
    </div>
};