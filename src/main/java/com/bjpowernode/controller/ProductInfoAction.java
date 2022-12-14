package com.bjpowernode.controller;

import com.bjpowernode.pojo.ProductInfo;
import com.bjpowernode.pojo.vo.ProductInfoVo;
import com.bjpowernode.service.ProductInfoService;
import com.bjpowernode.service.ProductTypeService;
import com.bjpowernode.service.impl.ProductInfoServiceImpl;
import com.bjpowernode.utils.FileNameUtil;
import com.github.pagehelper.PageInfo;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/prod")
public class ProductInfoAction {
    public static final int PAGE_SIZE = 5;
    String saveFileName = "";
    @Autowired
    ProductInfoService productInfoService;

    @RequestMapping("/getAll")
    public String getAll(HttpServletRequest request) {
        List<ProductInfo> list = productInfoService.getAll();
        request.setAttribute("list", list);
        return "product";
    }

    @RequestMapping("/split")
    public String split(HttpServletRequest request) {
        PageInfo pageInfo = null;
        Object vo = request.getSession().getAttribute("prodVo");
        if (vo != null) {
            pageInfo = productInfoService.splitPageVo((ProductInfoVo) vo, PAGE_SIZE);
            request.setAttribute("pname", ((ProductInfoVo) vo).getPname());
            request.setAttribute("hprice", ((ProductInfoVo) vo).getHprice());
            request.setAttribute("lprice", ((ProductInfoVo) vo).getLprice());
            request.getSession().removeAttribute("prodVo");
        } else {
            pageInfo = productInfoService.splitPage(1, PAGE_SIZE);
        }
        request.setAttribute("pb", pageInfo);
        return "product";
    }

    @RequestMapping("/ajaxSplit")
    @ResponseBody
    public void ajaxSplit(ProductInfoVo vo, HttpSession session) {
        PageInfo pageInfo = productInfoService.splitPageVo(vo, PAGE_SIZE);
        session.setAttribute("pb", pageInfo);
    }

    @ResponseBody
    @RequestMapping("/ajaxImg")
    public Object ajaxImg(MultipartFile pimage, HttpServletRequest request) {
        //?????????????????????UUID+??????????????????.jpg???.png
        saveFileName = FileNameUtil.getUUIDFileName() + FileNameUtil.getFileType(pimage.getOriginalFilename());
        //?????????????????????????????????
        String path = request.getServletContext().getRealPath("image_big");
        try {
            //??????
            pimage.transferTo(new File(path + File.separator + saveFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //???????????????json???????????????????????????????????????????????????????????????
        JSONObject object = new JSONObject();
        object.put("imgurl", saveFileName);
        return object.toString();
    }

    @RequestMapping("/save")
    public String save(ProductInfo info, HttpServletRequest request) {
        info.setpImage(saveFileName);
        info.setpDate(new Date());
        int num = -1;
        try {
            num = productInfoService.save(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (num > 0) {
            request.setAttribute("msg", "???????????????");
        } else {
            request.setAttribute("msg", "???????????????");
        }
        saveFileName = "";
        return "forward:/prod/split.action";
    }

    @RequestMapping("/one")
    public String one(int pid, ProductInfoVo vo, Model model, HttpSession session) {
        ProductInfo info = productInfoService.getById(pid);
        //???????????????????????????session????????????????????????????????????????????????????????????
        session.setAttribute("prodVo", vo);
        model.addAttribute("prod", info);
        return "update";
    }

    @RequestMapping("/update")
    public String update(ProductInfo info, HttpServletRequest request) {
        /*
        ??????ajax????????????????????????????????????????????????saveFileName????????????????????????????????????
        ????????????????????????ajax?????????????????????saveFileName="",
        ?????????info????????????????????????????????????pImage?????????????????????
        */
        if (!saveFileName.equals("")) {
            info.setpImage(saveFileName);
        }
        int num = -1;
        try {
            num = productInfoService.update(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (num > 0) {
            request.setAttribute("msg", "???????????????");
        } else {
            request.setAttribute("msg", "???????????????");
        }
        saveFileName = "";
        return "forward:/prod/split.action";
    }

    @RequestMapping("/delete")
    public String delete(int pid, HttpServletRequest request) {
        int num = -1;
        try {
            num = productInfoService.delete(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (num > 0) {
            request.setAttribute("msg", "???????????????");
        } else {
            request.setAttribute("msg", "???????????????");
        }
        return "forward:/prod/deleteAjaxSplit.action";
    }

    @RequestMapping(value = "/deleteAjaxSplit", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public Object deleteAjaxSplit(HttpServletRequest request) {
        PageInfo info = productInfoService.splitPage(1, PAGE_SIZE);
        request.getSession().setAttribute("pb", info);
        return request.getAttribute("msg");
    }

    @RequestMapping("/deleteBatch")
    public String deleteBatch(String pids, HttpServletRequest request) {
        String ids[] = pids.split(",");
        try {
            int num = productInfoService.deleteBatch(ids);
            if (num > 0) {
                request.setAttribute("msg", "?????????????????????");
            } else {
                request.setAttribute("msg", "?????????????????????");
            }
        } catch (Exception e) {
            request.setAttribute("msg", "?????????????????????");
            e.printStackTrace();
        }
        return "forward:/prod/deleteAjaxSplit.action";
    }

    @ResponseBody
    @RequestMapping("/condition")
    public void condition(ProductInfoVo vo, HttpSession session) {
        List<ProductInfo> list = productInfoService.selectCondition(vo);
        session.setAttribute("list", list);
    }

}
