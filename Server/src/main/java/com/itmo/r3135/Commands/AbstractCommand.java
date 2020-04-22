package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;

public abstract class AbstractCommand {
    protected Collection collection;
    protected Mediator serverWorker;

    public AbstractCommand(Collection collection, Mediator serverWorker) {
        this.collection = collection;
        this.serverWorker = serverWorker;
    }

    public abstract ServerMessage activate(Command command);
}
