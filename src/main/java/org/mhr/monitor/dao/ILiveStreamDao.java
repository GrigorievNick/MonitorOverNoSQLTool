package org.mhr.monitor.dao;


import org.mhr.monitor.model.Msg;
import org.mhr.monitor.model.OperationType;
import rx.Observable;

public interface ILiveStreamDao {

    Observable<Msg> find(OperationType type);

    Observable<Msg> find();
}
