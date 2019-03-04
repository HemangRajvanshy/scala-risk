let init() {
  let ws: Websocket.t(Js.t({.., _type: string})) = Websocket.make("ws://localhost:9000/ws");
  let token: option(string) = ref(None);
  Websocket.onOpen(ws, _ =>
    Websocket.onMessage(ws, msg => switch (msg##_type) {
      | "actors.Token" => {
        /*let msg: Js.t({. _type: String, token: String}) = Obj.magic(msg);*/
        token := Some(msg##token);
        Websocket.send(ws, {
          "_type": "actors.AssignName",
          "name": "Bob",
          "token": msg##token
        });
      }
      | "actors.Ping" =>
        Websocket.send(ws, {
          "_type": "actors.Pong",
          "msg": "Pong"
        })
    });
  );
};

ReactDOMRe.renderToElementWithId(<ChatBox you="Bob" />, "chatbox");
