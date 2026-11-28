package com.byahero.repository;

import com.byahero.model.RouteStop;

/** Full route-stop repository: the read-only lookups plus save/update/delete. */
public interface RouteRepository extends RouteReadRepository, WriteRepository<RouteStop> {
}
