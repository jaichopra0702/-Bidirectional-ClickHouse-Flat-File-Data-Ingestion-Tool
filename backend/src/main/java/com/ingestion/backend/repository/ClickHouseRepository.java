package com.ingestion.backend.repository;

import com.ingestion.backend.model.UkPricePaid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClickHouseRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<UkPricePaid> getUkPricePaidSamples() {
        String sql = "SELECT * FROM uk_price_paid LIMIT 10";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            UkPricePaid record = new UkPricePaid();
            record.setTransactionId(rs.getString("transaction_id"));
            record.setPrice(rs.getInt("price"));
            record.setDate(rs.getString("date"));
            record.setPostcode1(rs.getString("postcode1"));
            record.setPostcode2(rs.getString("postcode2"));
            record.setType(rs.getString("type"));
            record.setIsNew(rs.getInt("is_new"));
            record.setDuration(rs.getString("duration"));
            record.setAddr1(rs.getString("addr1"));
            record.setAddr2(rs.getString("addr2"));
            record.setStreet(rs.getString("street"));
            record.setLocality(rs.getString("locality"));
            record.setTown(rs.getString("town"));
            record.setDistrict(rs.getString("district"));
            record.setCounty(rs.getString("county"));
            record.setPpdCategoryType(rs.getString("ppd_category_type"));
            record.setRecordStatus(rs.getString("record_status"));
            return record;
        });
    }
}
