package org.mhr.monitor.dao;


import org.mhr.monitor.model.DataEvent;
import org.mhr.monitor.model.OperationType;
import rx.Observable;

public interface ILiveStreamDao {

    Observable<DataEvent> find(OperationType type);

    Observable<DataEvent> find();
}
