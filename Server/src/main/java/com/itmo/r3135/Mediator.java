package com.itmo.r3135;

import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;

public interface Mediator {
    ServerMessage processing(Command command);
}
