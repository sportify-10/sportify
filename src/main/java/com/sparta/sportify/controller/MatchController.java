package com.sparta.sportify.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.sportify.service.MatchService;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

	private MatchService matchService;

}
