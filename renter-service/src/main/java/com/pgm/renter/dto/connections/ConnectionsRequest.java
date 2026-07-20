package com.pgm.renter.dto.connections;

import java.util.List;

public record ConnectionsRequest(List<String> phoneNumbers, List<String> emails) {
}
