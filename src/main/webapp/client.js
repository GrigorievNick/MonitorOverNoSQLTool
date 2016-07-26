import moment from 'moment'
import React from 'react';
import ReactDOM from 'react-dom';
import { StreamAPI } from 'leap-js';
import { BehaviorSubject, Observable } from 'rxjs';
import { Table, MyRadioButtons, MyTimePickerStart, MyTimePickerEnd, NowButton,
    opTypeBehave, liveBehave, startDateBehave, endDateBehave } from './views';

const DLWS = new StreamAPI('WS', 'ws://admin:admin@localhost:8080/websocket/');
const mountNode = document.getElementById('content');
const switchNode = document.getElementById('switch');
const startNode = document.getElementById('start');
const endNode = document.getElementById('end');
const liveNode = document.getElementById('live');

opTypeBehave.subscribe(x => DLWS.send({"type": "Command", "command": "STOP"}))
liveBehave.subscribe(isLive => {
    if (isLive && startDateBehave.getValue()) {
        DLWS.send({"type": "Command", "command": "STOP"})
    } else if (startDateBehave.getValue() && endDateBehave.getValue()) {
        DLWS.send({"type": "Command", "command": "STOP"})
    }
});

startDateBehave.subscribe(x => {
    ReactDOM.render(new NowButton(false), liveNode)
})
//endDateBehave.subscribe(x => DLWS.send({"type": "Command", "command": "STOP"}))


const stopResponse = DLWS.dataStream
    .filter(x => x.type !== 'open')
    .map(x => JSON.parse(x.data))
    .filter(x => x.command === 'STOP_DONE')
    .map(x => [opTypeBehave.getValue(), liveBehave.getValue(), startDateBehave.getValue(), endDateBehave.getValue()])
    .map(([opType, isLive, startDate, endDate]) => {
        const newCmd = {"type": "Command", "command": "START", "operationType": opType};
        if (isLive && startDate) {
            newCmd.start = {date: moment(startDate).unix(), live: false}
            newCmd.end = {date: moment(endDate).unix(), live: true}
        } else if (startDate && endDate) {
            newCmd.start = {date: moment(startDate).unix(), live: false}
            newCmd.end = {date: moment(endDate).unix(), live: false}
        }
        return newCmd
    });

stopResponse.subscribe((cmd) => runSubscriber(cmd));


const runSubscriber = (value = {"type": "Command", "command": "START", "operationType": 'WIDTHRAW'}) => {
    DLWS.send(value)
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
        ReactDOM.render(new MyRadioButtons(), switchNode);
        ReactDOM.render(new NowButton(), liveNode);
        ReactDOM.render(new MyTimePickerStart(), startNode);
        ReactDOM.render(new MyTimePickerEnd(), endNode);
        runSubscriber();
    }, 2000);
};

init();
