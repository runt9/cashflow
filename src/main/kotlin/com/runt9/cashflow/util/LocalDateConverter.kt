package com.runt9.cashflow.util

import java.sql.Date
import java.time.LocalDate
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class LocalDateConverter : AttributeConverter<LocalDate, Date> {
    override fun convertToDatabaseColumn(localDate: LocalDate?): Date? = if (localDate == null) null else Date.valueOf(localDate)
    override fun convertToEntityAttribute(date: Date?): LocalDate? = date?.toLocalDate()
}