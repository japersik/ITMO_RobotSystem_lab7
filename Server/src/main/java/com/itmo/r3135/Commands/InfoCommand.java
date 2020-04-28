package com.itmo.r3135.Commands;

import com.itmo.r3135.DataManager;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;

public class InfoCommand extends AbstractCommand {

    public InfoCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {
        dataManager.getLock().readLock().lock();
        String s = dataManager.toString();
        dataManager.getLock().readLock().unlock();
        return new ServerMessage(s);
    }
}
