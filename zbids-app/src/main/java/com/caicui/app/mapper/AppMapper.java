package com.caicui.app.mapper;

import com.caicui.app.dmo.App;
import com.caicui.app.dmo.AppChecktokenLog;
import com.caicui.app.dmo.AppGettokenLog;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

@Mapper
public interface AppMapper {


    @Select({
            "<script>",
            "SELECT id,create_date,modify_date,app_id,app_key,app_type,app_levl,app_url,white_ip_list,state ",
            "FROM bit_app ",
            "WHERE app_id = #{appId,jdbcType=VARCHAR} and app_key = #{appKey,jdbcType=VARCHAR} and app_type = #{appType,jdbcType=VARCHAR}",
            "</script>"})
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.VARCHAR, id = true),
            @Result(column = "create_date", property = "createDate", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "modify_date", property = "modifyDate", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "app_id", property = "appId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_key", property = "appKey", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_type", property = "appType", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_levl", property = "appLevl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_url", property = "appUrl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "white_ip_list", property = "whiteIpList", jdbcType = JdbcType.VARCHAR)
    })
    List<App> selectApp(App p);


    @Select({
            "<script>",
            "SELECT id,create_date,modify_date,app_id,app_key,app_type,app_levl,app_url,white_ip_list,state ",
            "FROM bit_app ",
            "WHERE id = #{id,jdbcType=VARCHAR} ",
            "</script>"})
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.VARCHAR, id = true),
            @Result(column = "create_date", property = "createDate", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "modify_date", property = "modifyDate", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "app_id", property = "appId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_key", property = "appKey", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_type", property = "appType", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_levl", property = "appLevl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_url", property = "appUrl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "white_ip_list", property = "whiteIpList", jdbcType = JdbcType.VARCHAR)
    })
    App selectAppById(String id);


    @Insert({
            "insert into bit_app_checktoken_log(id,create_date,req_ip,token,info,state)",
            "values(#{id,jdbcType=VARCHAR},#{createDate,jdbcType=TIMESTAMP},#{reqIp,jdbcType=VARCHAR},",
            "#{token,jdbcType=VARCHAR},#{info,jdbcType=VARCHAR},#{state,jdbcType=VARCHAR})"})
    @SelectKey(before = true, keyProperty = "id", resultType = String.class, statement = {
            " select replace(UUID(),'-','') id"
    })
    int insertAppChecktokenLog(AppChecktokenLog record);


    @Insert({
            "insert into bit_app_gettoken_log(id,create_date,req_app_id,req_app_key,req_app_type,req_ip,state,app_id,token)",
            "values(#{id,jdbcType=VARCHAR},#{createDate,jdbcType=TIMESTAMP},#{reqAppId,jdbcType=VARCHAR},#{reqAppKey,jdbcType=VARCHAR},",
            "#{reqAppType,jdbcType=VARCHAR},#{reqIp,jdbcType=VARCHAR},#{state,jdbcType=VARCHAR},#{appId,jdbcType=VARCHAR},",
            "#{token,jdbcType=VARCHAR})"})
    @SelectKey(before = true, keyProperty = "id", resultType = String.class, statement = {
            " select replace(UUID(),'-','') id"
    })
    int insertAppGettokenLog(AppGettokenLog record);


    @Select({
            "<script>",
            "SELECT id,create_date,modify_date,app_id,app_key,app_type,app_levl,app_url,white_ip_list,state ",
            "FROM bit_app ",
            "WHERE app_id = #{appId,jdbcType=VARCHAR} ",
            "</script>"})
    @Results({
            @Result(column = "id", property = "id", jdbcType = JdbcType.VARCHAR, id = true),
            @Result(column = "create_date", property = "createDate", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "modify_date", property = "modifyDate", jdbcType = JdbcType.TIMESTAMP),
            @Result(column = "app_id", property = "appId", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_key", property = "appKey", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_type", property = "appType", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_levl", property = "appLevl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "app_url", property = "appUrl", jdbcType = JdbcType.VARCHAR),
            @Result(column = "white_ip_list", property = "whiteIpList", jdbcType = JdbcType.VARCHAR)
    })
    App selectAppByAppId(String appId);


}