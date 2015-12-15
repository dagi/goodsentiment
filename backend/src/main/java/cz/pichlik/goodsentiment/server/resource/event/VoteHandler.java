/*
 * Copyright 2015 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License");
 */

package cz.pichlik.goodsentiment.server.resource.event;

import static java.time.ZoneOffset.UTC;

import java.time.LocalDateTime;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import cz.pichlik.goodsentiment.server.ioc.ObjectFactory;

public class VoteHandler implements RequestHandler<VoteRequest, VoteResponse>{
    private VoteRepository voteRepository = new ObjectFactory().voteRepository();

    @Override
    public VoteResponse handleRequest(VoteRequest input, Context context) {
        input.setTimestamp(LocalDateTime.now().toEpochSecond(UTC));
        voteRepository.save(input);
        return new VoteResponse();
    }

    void setVoteRepository(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }
}
