package com.itmo.r3135.Commands;

import com.itmo.r3135.DataManager;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractCommand {
    protected DataManager dataManager;
    protected Mediator serverWorker;
    static final Logger logger = LogManager.getLogger("CommandWorker");

    public AbstractCommand(DataManager dataManager, Mediator serverWorker) {
        this.dataManager = dataManager;
        this.serverWorker = serverWorker;
    }

    public abstract ServerMessage activate(Command command);
}
