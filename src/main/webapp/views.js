import React from 'react';
import DataGrid from 'react-datagrid';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import {RadioButton, RadioButtonGroup} from 'material-ui/RadioButton';
import ActionFavorite from 'material-ui/svg-icons/action/favorite';
import ActionFavoriteBorder from 'material-ui/svg-icons/action/favorite-border';
import Datetime from 'react-datetime';
import Toggle from 'material-ui/Toggle';
import { BehaviorSubject } from 'rxjs';

const opTypeBehave = new BehaviorSubject();
const startDateBehave = new BehaviorSubject();
const endDateBehave = new BehaviorSubject();
const liveBehave = new BehaviorSubject();

const switchLive = (isLive) => liveBehave.next(isLive);
const switchFromTime = (dateTime) => startDateBehave.next(dateTime);
const switchToTime = (dateTime) => endDateBehave.next(dateTime);
const switchOp = (event, opType) => opTypeBehave.next(opType);


const MyRadioButtons = () => (
    <MuiThemeProvider>
        <div>
            <RadioButtonGroup defaultSelected="WIDTHRAW" onChange={switchOp}>
                <RadioButton
                    value="WIDTHRAW"
                    label="WIDTHRAW"
                />
                <RadioButton
                    value="DEPOSIT"
                    label="DEPOSIT"
                />
                <RadioButton
                    value="TRANSFER"
                    label="TRANSFER"
                />
            </RadioButtonGroup>
        </div>
    </MuiThemeProvider>
);

const MyTimePickerStart = () => (
        <div>
            <span>Start</span>
            <Datetime
                onChange={switchFromTime}
            />
        </div>
);

const MyTimePickerEnd = () => (
    <div>
        <span>End</span>
        <Datetime
            onChange={switchToTime}
        />
    </div>
);


const NowButton = (disable = true) => (
    <MuiThemeProvider>
        <div>
            <Toggle
                label="Live mode"
                onToggle={switchLive}
                defaultToggled={true}
                disabled={disable}
            />
        </div>
    </MuiThemeProvider>
);

const onColumnResize = (firstCol, firstSize) => {
    firstCol.width = firstSize
};
const pager = (data, query) =>
    data.slice(query.skip, query.skip + query.pageSize);

const Table = (data, isLoading = false) => {
    const columns = Object.keys(data[0]).map((x) => {
        return {name: x}
    });

    return (<DataGrid
        //idProperty={columns[0].name}
        dataSource={ (query) => Promise.resolve({data: pager(data, query), count: data.length}) }
        columns={columns}
        style={{height: 500}}
        emptyText={'No records'}
        pagination={true}
        defaultPageSize={10}
        paginationToolbarProps={{ pageSizes: [ 10 ] }}
        onColumnResize={onColumnResize}
        loading={isLoading}
    />)
};

export {Table, MyRadioButtons, MyTimePickerStart, MyTimePickerEnd, NowButton,
    opTypeBehave, liveBehave, startDateBehave, endDateBehave}

