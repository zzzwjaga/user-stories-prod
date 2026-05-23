package com.prod.user_stories_prod.responses;

public record InvestCheckResponce(int independentScore,
                                  int negotiableScore,
                                  int valuableScore,
                                  int estimableScore,
                                  int smallScore,
                                  int testableScore,
                                  String issues,
                                  String suggestions) {
}
