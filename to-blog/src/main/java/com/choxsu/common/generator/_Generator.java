

package com.choxsu.common.generator;

import com.choxsu.common.go.MyGenerator;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;

import javax.sql.DataSource;

/**
 * Model、BaseModel、_MappingKit 生成器
 *
 * @author Administrator
 */
public class _Generator {

    /**
     * 部分功能使用 Db + Record 模式实现，无需生成 model 的 table 在此配置
     * // 暂不实现该功能
     */
    private static String[] excludedTable = {
            "",
    };

    /**
     * 重用 JFinalClubConfig 中的数据源配置，避免冗余配置
     */
    public static DataSource getDataSource() {

        String url = "jdbc:mysql://192.168.3.45:3306/xilian168?characterEncoding=utf8&useSSL=false";
        String username = "root";
        String pwd = "xl168";
        DruidPlugin druidPlugin = new DruidPlugin(url, username, pwd);
//        DruidPlugin druidPlugin = StartConfig.getDruidPlugin();
        druidPlugin.start();
        return druidPlugin.getDataSource();
    }

    public static void main(String[] args) {
        // base model 所使用的包名
        String baseModelPackageName = "com.choxsu.common.generator.entity.base";
        // base model 文件保存路径
        String baseModelOutputDir = PathKit.getWebRootPath()
                + "/src/main/java/com/choxsu/common/generator/entity/base";

        System.out.println("输出路径：" + baseModelOutputDir);

        // model 所使用的包名 (MappingKit 默认使用的包名)
        String modelPackageName = "com.choxsu.common.generator.entity";
        // model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
        String modelOutputDir = baseModelOutputDir + "/..";

        String controllerGeneratorOutputDir = PathKit.getWebRootPath() + "/src/main/java/com/choxsu/controller";
        String serviceGeneratorOutputDir = PathKit.getWebRootPath() + "/src/main/java/com/choxsu/service";

        // 创建生成器
        MyGenerator gen = new MyGenerator(getDataSource(),
                baseModelPackageName,
                baseModelOutputDir,
                modelPackageName,
                modelOutputDir,
                controllerGeneratorOutputDir,
                serviceGeneratorOutputDir);


        gen.setGenerateService(true);

        // 设置数据库方言
        gen.setDialect(new MysqlDialect());

        //设置Mapping生成的文文件名
        gen.setMappingKitClassName("_MappingKit");

        /**
         * 设置 BaseModel 是否生成链式 setter 方法
         */
        gen.setGenerateChainSetter(false);

        //设置自定义表生成

        gen.setMetaBuilder(new _MetaBuilder(getDataSource()));
        // 添加不需要生成的表名
        for (String table : excludedTable) {
            gen.addExcludedTable(table);
        }
        // 设置是否在 Model 中生成 dao 对象
        gen.setGenerateDaoInModel(false);
        // 设置是否生成字典文件
        gen.setGenerateDataDictionary(false);

        //是否生成service
        gen.setGenerateService(false);
        // 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为 "User"而非 OscUser
//        gen.setRemovedTableNamePrefixes("sm_");
        // 生成
        gen.generate();
    }
}
