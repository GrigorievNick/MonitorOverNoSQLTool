const React = require('react')
const ReactDOM = require('react-dom')
const ReactPivot = require('react-pivot')
const mountNode = document.getElementById('content');


const StreamAPI = require('leap-js').StreamAPI;
const DLWS = new StreamAPI('WS', 'ws://admin:admin@localhost:8080/websocket/');



setTimeout(() => {
    DLWS.send({"type": "Command", "command": "START"})
}, 2000)

//.skip handshake
DLWS.dataStream
    .skip(1)
    .take(100)
    .map(x => JSON.parse(x.data))
    .filter(x => x.type === 'Data')
    .do(x => console.log(x))
    .map(x => x.msg.operationBody)
    .map(x =>
        Object.keys(x).map((x, y) => {
            return {title: x, data: y}
        })
    )
    .subscribe(data => {
        const dimensions = data.map(x => { return {value: x.title, title: x.title}})

        ReactDOM.render(
            <ReactPivot rows={data}
                        dimensions={dimensions}
                        calculations={[]}
                        activeDimensions={dimensions.map(x => x.title)}
        />,
            mountNode
        )
    }, () => void 0, () => DLWS.send({"type": "Command", "command": "STOP"}))

