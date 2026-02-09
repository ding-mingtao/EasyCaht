package com.easychat.entity.vo;

/**
 * @ClassName: AppUpdateVO
 * @Description: App发更新信息
 * @Author: 丁铭涛
 * @DateTime: 2025/6/1 12:50
 **/

import java.io.Serializable;
import java.util.List;

/**
 * app发布
 */
public class AppUpdateVO implements Serializable {

    private static final long serialVersionUID = 4756060542150096340L;
    private Integer id;

    /**
     * 版本号
     */
    private String version;

    /**
     * 更新描述
     */
    private List<String> updateList;

    private Long size;

    private String fileName;

    private Integer fileType;

    private String outerLink;

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getUpdateList() {
        return updateList;
    }

    public void setUpdateList(List<String> updateList) {
        this.updateList = updateList;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOuterLink() {
        return outerLink;
    }

    public void setOuterLink(String outerLink) {
        this.outerLink = outerLink;
    }
}
