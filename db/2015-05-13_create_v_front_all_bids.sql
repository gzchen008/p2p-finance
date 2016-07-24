CREATE OR REPLACE VIEW
    v_front_all_bids
    (
        id,
        credit_name,
        credit_image_filename,
        product_filename,
        product_name,
        show_type,
        title,
        amount,
        status,
        user_id,
        period,
        apr,
        is_hot,
        period_unit,
        is_agency,
        agency_name,
        has_invested_amount,
        bid_image_filename,
        small_image_filename,
        loan_schedule,
        bonus_type,
        bonus,
        repayment_time,
        no,
        award_scale,
        repayment_type_id,
        repay_name,
        is_show_agency_name,
        product_id,
        num,
        credit_level_id,
        TIME,
        min_invest_amount
    ) AS
SELECT
    t_bids.id                       AS id,
    t_credit_levels.name            AS credit_name,
    t_credit_levels.image_filename  AS credit_image_filename,
    t_products.name_image_filename  AS product_filename,
    t_products.name                 AS product_name,
    t_bids.show_type                AS show_type,
    t_bids.title                    AS title,
    t_bids.amount                   AS amount,
    t_bids.status                   AS status,
    t_bids.user_id                  AS user_id,
    t_bids.period                   AS period,
    t_bids.apr                      AS apr,
    t_bids.is_hot                   AS is_hot,
    t_bids.period_unit              AS period_unit,
    t_bids.is_agency                AS is_agency,
    t_agencies.name                 AS agency_name,
    t_bids.has_invested_amount      AS has_invested_amount,
    t_bids.image_filename           AS bid_image_filename,
    t_products.small_image_filename AS small_image_filename,
    t_bids.loan_schedule            AS loan_schedule,
    t_bids.bonus_type               AS bonus_type,
    t_bids.bonus                    AS bonus,
    (
        SELECT
            t_bills.repayment_time AS repayment_time
        FROM
            t_bills
        WHERE t_bids.id = t_bills.bid_id
        AND t_bills.status = -1
        GROUP BY t_bills.bid_id ) AS repayment_time,
    concat(
    (
        SELECT
            t_system_options._value AS _value
        FROM
            t_system_options
        WHERE
                t_system_options._key = 'loan_number'),t_bids.id + '') AS no,
    t_bids.award_scale              AS award_scale,
    t_bids.repayment_type_id        AS repayment_type_id,
    t_dict_bid_repayment_types.name AS repay_name,
    t_bids.is_show_agency_name      AS is_show_agency_name,
    t_products.id                   AS product_id,
    f_credit_levels(t_users.id)     AS num,
    t_users.credit_level_id         AS credit_level_id,
    t_bids.time                     AS time,
    t_bids.min_invest_amount        AS min_invest_amount
FROM
    t_bids
LEFT JOIN t_products ON t_products.id = t_bids.product_id
LEFT JOIN t_users ON t_users.id = t_bids.user_id
LEFT JOIN t_credit_levels ON t_credit_levels.id = t_users.credit_level_id
LEFT JOIN t_agencies ON t_agencies.id = t_bids.agency_id
LEFT JOIN t_dict_bid_repayment_types ON t_dict_bid_repayment_types.id = t_bids.repayment_type_id
WHERE t_bids.status IN (1,2,3,4,5)
ORDER BY t_bids.loan_schedule, t_bids.is_hot DESC, t_bids.id DESC ;