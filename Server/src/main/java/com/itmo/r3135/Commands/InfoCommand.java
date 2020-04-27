package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;

public class InfoCommand extends AbstractCommand {

    public InfoCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {
        collection.getLock().readLock().lock();
        String s = collection.toString();
        collection.getLock().readLock().unlock();
        return new ServerMessage(s);
    }
}
