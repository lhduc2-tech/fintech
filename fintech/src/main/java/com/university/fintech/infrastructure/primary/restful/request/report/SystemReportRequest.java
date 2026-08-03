package com.university.fintech.infrastructure.primary.restful.request.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemReportRequest {

    private OffsetDateTime from;
    private OffsetDateTime to;
}
