package com.caicui.app.mapper;

import com.caicui.app.dmo.AppLoginLog;
import org.apache.ibatis.annotations.*;

import java.util.List;


@Mapper
public interface AppLoginLogMapper {
    @Insert({
            "insert into bit_login_log(id,create_date,app_version,client_type,ip,login_time,logout_time,phoneid,phone_type,system_type,system_version,token_id,member_id)",
            "values(#{id,jdbcType=VARCHAR},#{createDate,jdbcType=TIMESTAMP},#{appVersion,jdbcType=VARCHAR},#{clientType,jdbcType=VARCHAR},",
            "#{ip,jdbcType=VARCHAR},#{loginTime,jdbcType=TIMESTAMP},#{logoutTime,jdbcType=TIMESTAMP},#{phoneID,jdbcType=VARCHAR},",
            "#{phoneType,jdbcType=VARCHAR},#{systemType,jdbcType=VARCHAR},#{systemVersion,jdbcType=VARCHAR},#{tokenId,jdbcType=VARCHAR},#{memberId,jdbcType=VARCHAR})"})
    @SelectKey(before = true, keyProperty = "id", resultType = String.class, statement = {
            " select replace(UUID(),'-','') id"
    })
    int insertAppChecktokenLog(AppLoginLog record);


    @Select({
            "<script>",
            "SELECT id,create_date,app_version,client_type,ip,login_time,logout_time,phoneid,phone_type,system_type,system_version,token_id,member_id ",
            "FROM bit_login_log ",
            "WHERE client_type != 'admin' and member_id = #{memberId,jdbcType=VARCHAR} ",
            "ORDER BY login_time DESC ",
            "<if test='startNo != -1 and pageSize != -1 '> limit #{startNo,jdbcType=INTEGER},#{pageSize,jdbcType=INTEGER}</if>",
            "</script>"})
    List<AppLoginLog> selectAppLoginLog(@Param("memberId") String memberId,
                                        @Param("startNo") Integer pageIndex,
                                        @Param("pageSize") Integer pageSize);


    @Select({
            "<script>",
            "SELECT count(1) ",
            "FROM bit_login_log ",
            "WHERE client_type != 'admin' and member_id = #{memberId,jdbcType=VARCHAR} ",
            "</script>"})
    int selectAppLoginLogCount(String memberId);

}