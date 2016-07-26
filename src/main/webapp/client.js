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
var columns = [];

//.skip handshake
DLWS.dataStream
    .skip(1)
    //.take(5)
    .map(x => JSON.parse(x.data))
    .filter(x => x.type === 'Data')
    //.do(x => console.log(x))
    .map(x => x.msg.operationBody)
    .subscribe(data => {
        if (typeof columns === 'undefined' || columns.length <= 0) {
            columns = Object.keys(data).map((x) => { return {name: x} });
        }
        msgArr.push(data);
        //function dataSource
        ReactDOM.render(
            <DataGrid
                idProperty={columns[0].name}
                dataSource={(query) => { return msgArr.slice(query.skip, query.pageSize); }}
                columns={columns}
                style={{height: 500}}
                //withColumnMenu={false}
                emptyText={'No records'}
                pagination={true}
                defaultPageSize={10}
                paginationToolbarProps={{
				pageSizes: [
				    10,
					100,
					1000,
					2000
				]
			}}
            />,
            mountNode
        )
    }, () => void 0, () => DLWS.send({"type": "Command", "command": "STOP"}));
