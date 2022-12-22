package com.atguigu.gmall.sms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuFullReductionMapper reductionMapper; // 3.2 保存 sms_sku_full_reduction

    @Autowired
    private SkuLadderMapper ladderMapper; // 3.3 保存 sms_sku_ladder

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    @Transactional // 分支事务 RM
    public void saveSales(SkuSaleVo saleVo) {

        // 3.1 保存积分优惠表
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(saleVo, skuBoundsEntity); // 源 -> 对象
        List<Integer> work = saleVo.getWork();
        if (CollectionUtils.isNotEmpty(work))
            skuBoundsEntity.setWork(
                    work.get(3) * 8 + work.get(2) * 4 + work.get(1) * 2 + work.get(0)
            );
        save(skuBoundsEntity);

        // 3.2 保存 sms_sku_full_reduction
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(saleVo, reductionEntity);
        reductionEntity.setAddOther(saleVo.getFullAddOther());
        reductionMapper.insert(reductionEntity);

        // 3.3 保存 sms_sku_ladder
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(saleVo, ladderEntity);
        ladderEntity.setAddOther(saleVo.getLadderAddOther());
        ladderMapper.insert(ladderEntity);
    }

    @Override
    public List<ItemSaleVo> querySalesBySkuId(Long skuId) {
        List<ItemSaleVo> itemSaleVos = new ArrayList<>();

        // 把积分优惠封装成 itemSaleVo 对象 放入集合
        SkuBoundsEntity skuBoundsEntity = getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        // 如果营销信息不为空
        if (skuBoundsEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();
            itemSaleVo.setSaleId(skuBoundsEntity.getId());
            itemSaleVo.setType("积分");
            itemSaleVo.setDesc("送 "
                    + skuBoundsEntity.getGrowBounds() + " 成长积分, 送 "
                    + skuBoundsEntity.getBuyBounds() + " 购物积分"
            );

            itemSaleVos.add(itemSaleVo);
        }

        // 满减优惠
        SkuFullReductionEntity reductionEntity = reductionMapper.selectOne(
                new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId)
        );
        if (reductionEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();

            itemSaleVo.setSaleId(reductionEntity.getId());
            itemSaleVo.setType("满减");
            itemSaleVo.setDesc("满 "
                    + reductionEntity.getFullPrice() + " 减 "
                    + reductionEntity.getReducePrice() + " 元"
            );

            itemSaleVos.add(itemSaleVo);
        }

        // 打折优惠
        SkuLadderEntity ladderEntity = ladderMapper.selectOne(
                new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId)
        );
        if (ladderEntity != null) {
            ItemSaleVo itemSaleVo = new ItemSaleVo();

            itemSaleVo.setSaleId(ladderEntity.getId());
            itemSaleVo.setType("打折");
            itemSaleVo.setDesc("满 "
                    + ladderEntity.getFullCount() + " 件, 打 "
                    + ladderEntity.getDiscount().divide(new BigDecimal(10)) + " 折"
            );

            itemSaleVos.add(itemSaleVo);
        }

        return itemSaleVos;
    }

}