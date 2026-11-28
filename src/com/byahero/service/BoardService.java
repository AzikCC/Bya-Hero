package com.byahero.service;

import com.byahero.model.Bus;

import java.util.List;
import java.util.Map;

/** What the public board needs: the buses that are still in the terminal. */
public interface BoardService {

    /** Active buses (not DEPARTED) grouped by destination, in the order first seen. */
    Map<String, List<Bus>> getActiveBusesByDestination();
}
