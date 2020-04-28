package com.itmo.r3135.Commands;

import com.itmo.r3135.DataManager;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;

public abstract class AbstractCommand {
    protected DataManager dataManager;
    protected Mediator serverWorker;

    public AbstractCommand(DataManager dataManager, Mediator serverWorker) {
        this.dataManager = dataManager;
        this.serverWorker = serverWorker;
    }

    public abstract ServerMessage activate(Command command);
}
