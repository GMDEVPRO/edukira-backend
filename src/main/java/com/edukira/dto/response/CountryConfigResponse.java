package com.edukira.dto.response;

import com.edukira.enums.Currency;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CountryConfigResponse {
    private String countryCode;
    private String countryName;
    private Currency currency;
    private String timezone;
    private int academicYearStartMonth;
    private int gradingMax;
    private boolean supportsWave;
    private boolean supportsOrangeMoney;
    private boolean supportsMtnMomo;
    private boolean supportsAirtelMoney;
    private boolean supportsMpesa;
    private String phonePrefix;
}
