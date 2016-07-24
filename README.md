#项目搭建步骤：

## 拉项目下来：

git@gitlab.sunlights.me:sunlights/sp2ponline-dev-v2.git
	
## 环境变量：

jdk1.6  play-1.2.7

## 导入所有jar包：

* 导入play-1.2.7\framework\lib下所有jar包
* lib下所有jar包
* play-1.2.7\framework\play-1.2.7.jar包

## 配置运行参数

1. eclipse中：
	
	cmd
	
	cd project path
	
	play eclipsify

	配置java application
	
	sp2ponline-dev-v2项目配置
	name:sp2ponline-dev-v2
	project:sp2ponline-dev-v2
	main class:play.server.Server
	arguments: -Xms512m -Xmx512m -XX:PermSize=512m -XX:MaxPermSize=512m -Xms512m -Xmx512m -XX:PermSize=126m -XX:MaxPermSize=126m -Xdebug -Dplay.debug=yes -Dplay.id= -Dapplication.path="${project_loc:sp2ponline-dev-v2}" -Djava.endorsed.dirs="C:\Program Files\play-1.2.7\framework/endorsed" -javaagent:"C:\Program Files\play-1.2.7\framework/play-1.2.7.jar"
	
	p2pspay-dev-v2项目配置：
	name:p2pspay-dev-v2
	project:p2pspay-dev-v2
	main class:play.server.Server
	arguments:	-Xdebug  -Dplay.id= -Dapplication.path="${project_loc:p2pspay-dev-v2}" -Djava.endorsed.dirs="C:\Program Files\play-1.2.7/framework/endorsed" -javaagent:"C:\Program Files\play-1.2.7/framework/play-1.2.7.jar"

	
2. IDEA中	：

	 生成idea的项目：play idealize
	
	 Edit configurations
	 
	 add Application
	 
		```Main Class:play.server.Server```
		
		```VM options:-Dapplication.path="."```
	 
	 若出现版本问题  VerifyError...XXX   使用 play clean 删除tmp文件

## 运行

1. 本地:play run

2. dev:play run --%dev

3. test:play run --%test

4. local:play run --%local

## 打包

1. 本地:play war -o myapp.war

2. dev:play war -o myapp.war --%dev

3. test:play war -o myapp.war --%test

4. local:play war -o myapp.war --%local



        
        
		
		
		