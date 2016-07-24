package controllers.supervisor.activity.service;

import business.PageVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.supervisor.activity.vo.RedPacketVo;
import models.RedPacketModel;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import play.db.jpa.JPA;

import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Yuan on 2015/6/16.
 */
public class RedPacketService {

    private RedPacketBillService redPacketBillService = new RedPacketBillService();

    public void save(RedPacketVo redPacketVo) {
        RedPacketModel redPacketModel = redPacketVo.convertToRedPacket();
        redPacketModel.save();

    }

    public List<RedPacketVo> findRedPacketVos(PageVo pageVo) {
        int index = pageVo.getIndex();
        int pageSize = pageVo.getPageSize();
        StringBuffer jpql = new StringBuffer();
        jpql.append(" select new ");
        jpql.append(RedPacketVo.class.getName() + "(r)");
        jpql.append(" from RedPacketModel r");
        jpql.append(" where 1=1");

        Query query = JPA.em().createQuery(jpql.toString());
        query.setFirstResult(index);
        if (pageSize > 0) {
            query.setMaxResults(pageSize);
        }

        List<RedPacketVo> resultList = query.getResultList();

        return resultList;
    }

    public RedPacketVo findRedPacketDetailById(Long id) {
        if (id == null) return null;
        StringBuffer jpql = new StringBuffer();
        jpql.append(" select new ");
        jpql.append(RedPacketVo.class.getName() + "(r)");
        jpql.append(" from RedPacketModel r");
        jpql.append(" where 1=1");
        jpql.append(" and r.id = ").append(id);
        List<RedPacketVo> list = JPA.em().createQuery(jpql.toString()).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }


    public RedPacketVo findRedPacketVoBy(Long id, String openId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT");
        sql.append(" tp.id as \"id\"");
        sql.append(" ,tp.total as \"total\"");
        sql.append(" ,tp.total-COALESCE(rps.red_packet_amount,0) as \"balance\"");
        sql.append(" ,COALESCE(rps.red_packet_amount,0) as \"send\"");
        sql.append(" ,tp.create_time as \"createTime\"");
        sql.append(" ,tp.min_value as \"minValue\"");
        sql.append(" ,tp.max_value as \"maxValue\"");
        sql.append(" ,tp.total_num as \"totalNum\"");
        sql.append(" ,COALESCE(rps.red_packet_count,0)  as \"sendNum\"");
        sql.append(" ,tp.act_name as \"actName\"");
        sql.append(" ,tp.remark as \"remark\"");
        sql.append(" ,tp.content as \"content\"");
        sql.append(" ,tp.logo_img_url as \"logoImgUrl\"");
        sql.append(" ,tp.share_img_url as \"shareImgUrl\"");
        sql.append(" ,tp.share_img_url as \"shareImgUrl\"");
        sql.append(" ,tp.share_url as \"shareUrl\"");
        sql.append(" ,tp.wishing as \"wishing\"");
        sql.append(" ,tp.status as \"status\"");
        sql.append(" ,tp.couple as \"couple\"");
        sql.append(" ,COALESCE(ups.user_red_packet_count,0) as \"userCouple\"");
        sql.append(" FROM t_red_packet tp");
        sql.append(" LEFT JOIN (SELECT rpb.red_packet_id as red_packet_id, COUNT(1) AS red_packet_count ,SUM(rpb.amount) AS red_packet_amount FROM t_red_packet_bill rpb GROUP BY rpb.red_packet_id) rps");
        sql.append(" ON tp.id = rps.red_packet_id");
        sql.append(" LEFT JOIN (SELECT rpb.red_packet_id as red_packet_id,COUNT(1) AS user_red_packet_count FROM t_red_packet_bill rpb WHERE  rpb.open_id = '" + openId + "' GROUP BY rpb.red_packet_id) ups");
        sql.append(" ON tp.id = ups.red_packet_id");
        sql.append(" WHERE 1=1");
        sql.append(" AND tp.id = ").append(id);
        sql.append(" AND tp.status = 1");
        Query nativeQuery = JPA.em().createNativeQuery(sql.toString());
        nativeQuery.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        List<Map<String, Object>> resultList = nativeQuery.getResultList();

        if (!resultList.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            RedPacketVo redPacketVo = objectMapper.convertValue(resultList.get(0), RedPacketVo.class);
            if (redPacketVo.getSend().compareTo(redPacketVo.getTotal()) >= 0) {
               throw new RuntimeException("[红包金额已发完]");
            }
            if (redPacketVo.getSendNum().compareTo(redPacketVo.getTotalNum()) >= 0) {
               throw new RuntimeException("[红包个数已发完]");
            }
            if (redPacketVo.getUserCouple().compareTo(redPacketVo.getCouple()) >= 0) {
               throw new RuntimeException("[用户红包个数已发完]");
            }
            return redPacketVo;
        } else {
            return null;
        }

    }


}
