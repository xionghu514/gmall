package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();

        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("phone", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
            default:
                return null;
        }

        return userMapper.selectCount(wrapper) == 0;
    }

    @Override
    public void register(UserEntity userEntity, String code) {
        // TODO: 校验验证码 code 和 redis 中的 code 比较

        // 2. 生成盐
        String salt = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        userEntity.setSalt(salt);

        // 3. 加盐加密
        userEntity.setPassword(DigestUtils.md5Hex(userEntity.getPassword() + salt));

        // 设置创建时间等
        userEntity.setLevelId(1l);
        userEntity.setNickname(userEntity.getUsername());
        userEntity.setSourceType(1);
        userEntity.setIntegration(1000);
        userEntity.setGrowth(1000);
        userEntity.setStatus(1);
        userEntity.setCreateTime(new Date());

        // 4. 新增用户
        save(userEntity);

        // TODO: 删除短信验证码

    }

    /**
     * loginName 可以是 邮箱、手机号、名称
     *      1. 根据登陆名查询用户
     *      2. 对用户输入的明文密码加盐加密
     *      3. 比较数据库中的密文密码 和 上一步处理的密码. 一致才返回用户信息
     * @param loginName 默认用户 ikun
     * @param password  默认密码 cxk520
     * @return
     */
    @Override
    public UserEntity queryUser(String loginName, String password) {
        // 1. 根据登陆名查询用户
        UserEntity userEntity = getOne(
                new QueryWrapper<UserEntity>()
                        .eq("username", loginName).or()
                        .eq("phone", loginName).or()
                        .eq("email", loginName)
        );

        // 登陆名输出错误
        if (userEntity == null) {
            return userEntity;
        }

        // 2. 对用户输入的明文密码加盐加密
        password = DigestUtils.md5Hex(password + userEntity.getSalt()); // 密文密码

        // 3. 比较数据库中的密文密码 和 上一步处理的密码. 一致才返回用户信息
        if (StringUtils.equals(password, userEntity.getPassword())) {
            return userEntity;
        }

        return null;
    }

}