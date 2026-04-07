package com.ukproptech.api.common;

/**
 * Офіційний рейтинг шкіл Великої Британії за системою Ofsted.
 * Використовується для аналітики районів (Area Analytics)
 * та прийняття рішень про релокацію.
 */
public enum SchoolRating {
    /**
     * Outstanding (Найвища оцінка): Школа демонструє виняткові результати.
     */
    OUTSTANDING,

    /**
     * Good (Добре): Школа працює на належному рівні.
     */
    GOOD,

    /**
     * Requires Improvement (Потребує вдосконалення): Школа не відповідає всім стандартам.
     */
    REQUIRES_IMPROVEMENT,

    /**
     * Inadequate (Недостатньо): Школа має серйозні недоліки.
     */
    INADEQUATE,

    /**
     * Unknown/Pending: Якщо дані про школу ще не доступні або очікується перевірка.
     */
    UNKNOWN
}