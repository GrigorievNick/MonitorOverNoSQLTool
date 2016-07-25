const React = require('react');
const ReactDOM = require('react-dom');
const DataGrid = require('react-datagrid');

const mountNode = document.getElementById('content');

const StreamAPI = require('leap-js').StreamAPI;
const DLWS = new StreamAPI('WS', 'ws://admin:admin@localhost:8080/websocket/');

// Because of bug in lib
setTimeout(() => {
    DLWS.send({"type": "Command", "command": "START"})
}, 2000)

var msgArr = [];
const columns = []

//.skip handshake
DLWS.dataStream
    .skip(1)
    .take(5)
    .map(x => JSON.parse(x.data))
    .filter(x => x.type === 'Data')
    //.do(x => console.log(x))
    .map(x => x.msg.operationBody)
    .map(x =>
        Object.keys(x).map((x, y) => {
            return {title: x, data: y}
        })
    )
    .subscribe(data => {
        columns.push(data.map(x => {
            return {name: x.title}
        }));
        msgArr.push(data)

        ReactDOM.render(
            <DataGrid
                idProperty='_id'
                dataSource={msgArr}
                columns={columns}
                style={{height: 500}}
                //withColumnMenu={false}
            />,
            mountNode
        )
    }, () => void 0, () => DLWS.send({"type": "Command", "command": "STOP"}));

