import React from 'react';
import ReactDOM from 'react-dom';
import { StreamAPI } from 'leap-js';
import { Subject, Observable } from 'rxjs';
import { Table, MyRadioButtons } from './views';

const DLWS = new StreamAPI('WS', 'ws://admin:admin@localhost:8080/websocket/');
const mountNode = document.getElementById('content');
const switchNode = document.getElementById('switch');


const stopReq = new Subject();
const stopResponse = DLWS.dataStream
    .filter(x => x.type !== 'open')
    .map(x => JSON.parse(x.data))
    .combineLatest(stopReq.asObservable())
    .filter(([x,value]) => x.command === 'STOP_DONE')
    .do(([x,value]) => runSubscriber(value));

const sendCmd = (event, value) => {
    DLWS.send({"type": "Command", "command": "STOP"});
    stopReq.next(value);
};

const runSubscriber = (value = 'WIDTHRAW') => {
    DLWS.send({"type": "Command", "command": "START", "operationType": value })
    DLWS.dataStream
        .filter(x => x.type !== 'open')
        .takeUntil(stopResponse)
        .map(x => JSON.parse(x.data))
        .filter(x => x.type === 'Data')
        .map(x => x.operationBody)
        .scan((acc, delta) => [delta, ...acc], [])
        .subscribe(data => {
            const ref = ReactDOM.render(new Table(data), mountNode);
            ref.reload();
        }, () => void 0, () => {
            const ref = ReactDOM.render(new Table([{dummy: ''}], true), mountNode);
            ref.gotoPage(1);
        });
};

const init = () => {
    // Because of bug in lib
    setTimeout(() => {
        ReactDOM.render(new MyRadioButtons(sendCmd), switchNode);
        runSubscriber();
    }, 2000);
};

init();
