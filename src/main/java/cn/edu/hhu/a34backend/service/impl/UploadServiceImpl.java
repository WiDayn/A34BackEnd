package cn.edu.hhu.a34backend.service.impl;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import cn.edu.hhu.a34backend.param.UploadParam;
import cn.edu.hhu.a34backend.service.IndexService;
import cn.edu.hhu.a34backend.service.UploadService;
import cn.edu.hhu.a34backend.utils.PDFUtils;
import cn.edu.hhu.a34backend.utils.SnowFlake;
import cn.edu.hhu.a34backend.vo.ErrorCode;
import cn.edu.hhu.a34backend.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Component
@Slf4j
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    @Value("${setting.pdf-file-path}")
    private String uploadPath;

    @Value("${setting.pdf-temp-path}")
    private String tempPath;

    @Value("${setting.work-id}")
    private int workerId;

    @Value("${setting.datacenter-id}")
    private int datacenterId;

    @Autowired
    IndexService indexService;


    @Override
    public Result uploadPDF(UploadParam uploadParam) {
        SnowFlake snowFlake=new SnowFlake(workerId,datacenterId,1);

        String saveName = String.valueOf(snowFlake.nextId());

        String saveFilePath = uploadPath + "/" + saveName + ".pdf";

        BASE64Decoder base64Decoder = new BASE64Decoder();

        try {
            byte[] decodedBytes = base64Decoder.decodeBuffer(uploadParam.getData());

            File file = new File(saveFilePath);

            FileOutputStream fop = new FileOutputStream(file);

            fop.write(decodedBytes);

            fop.flush();

            fop.close();

            PDFUtils.divide(saveFilePath, saveName, tempPath);

        } catch (Exception e) {
            Result.fail(ErrorCode.SYSTEM_EXCEPTION.getCode(), ErrorCode.SYSTEM_EXCEPTION.getMsg());

            e.printStackTrace();
        }

        return Result.success(null, "Success");
    }

    //测试用
    //分割pdf并逐页添加至es的index
    @Override
    public Result uploadPDF2(UploadParam uploadParam) throws IOException
    {
        SnowFlake snowFlake = new SnowFlake(workerId, datacenterId, 1);

        long uuid=snowFlake.nextId();
        String saveName = String.valueOf(uuid);

        String saveFilePath = uploadPath + "/" + saveName + ".pdf";

        BASE64Decoder base64Decoder = new BASE64Decoder();
        byte[] decodedBytes = base64Decoder.decodeBuffer(uploadParam.getData());

        File file = new File(saveFilePath);

        FileOutputStream fop = new FileOutputStream(file);

        fop.write(decodedBytes);

        fop.flush();

        fop.close();

        String[] pdfPagesText=PDFUtils.split(saveFilePath, saveName, tempPath);

        int pageCnt=0;

        BASE64Encoder base64Encoder=new BASE64Encoder();
        System.out.println("ok");
        for(String pdfSinglePageText: pdfPagesText)
        {
            System.out.println("=======Page"+ ++pageCnt+"========");
            System.out.println(pdfSinglePageText);
            indexService.indexSinglePdfPage(uuid, pageCnt, pdfSinglePageText);
        }

        return Result.success(null, "Success");
    }
}
