CREATE OR REPLACE VIEW v_user_invest_info AS

select
u.id as user_id,
(select sum(a.receive_corpus + a.receive_interest + a.overdue_fine) from t_bill_invests a where a.user_id = u.id) as all_amounts,
(select sum(a.receive_interest + a.overdue_fine) from t_bill_invests a, t_bids b where a.bid_id = b.id and a.user_id = u.id and b.main_type_id <> 1) as stable_amounts,
(select sum(a.receive_interest + a.overdue_fine) from t_bill_invests a, t_bids b where a.bid_id = b.id and a.user_id = u.id and b.main_type_id = 1) as float_amounts,
(select sum(a.receive_interest + a.overdue_fine) from t_bill_invests a where a.user_id = u.id and a.status in (-1, -2,-5,-6)) as current_income_amounts,
(select sum(a.receive_interest + a.overdue_fine) from t_bill_invests a where a.user_id = u.id and a.status in (-3 ,-4, 0)) as history_income_amounts
from t_users u
group by u.id;









CREATE OR REPLACE VIEW v_bid_invest_info AS

select
a.id as id,
a.bid_id as bid_id,
b.title as title,
0 as main_type_id,
a.receive_corpus as invest_amounts,
 (a.receive_interest + a.overdue_fine) AS income_amounts,
 a.status as status,
 b.repayment_time as repayment_time,
 0 as quotient,
 0 as netvalue

from t_bill_invests a, t_bids b, t_users u
where a.bid_id = b.id
and  a.user_id = u.id