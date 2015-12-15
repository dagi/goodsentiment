/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.resource.event;

public interface VoteRepository {

    public void save(VoteRequest request);
}
