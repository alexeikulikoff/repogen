
package repogen


class App {
	static def appPath = '/home/admin2/eclipse-workspace/sys101_loader/src/main/java/com/cis/sys101/loader/'

	static def domainPath = appPath + 'domain'

	static def serviceConstantsPath = appPath + 'config/ServiceConstants.java'
	static def repositoryPath = appPath + 'repository/'

	String getDescription(String name) {
		def result
		def reader =  new BufferedReader(new FileReader(name));
		def line
		while((line = reader.readLine())!= null) {
			if (line.contains("@ApiModel") & line.contains("description")){
				def ds = line.split("=")
				if (ds != null && ds.size()>0) {
					result = ds[1].replace(')', '').replace('"', '')
				}
			}
		}
		return result
	}
	void createRepository() {
		def domains = new File(domainPath)
		domains.eachFile{
			def entName = it.getName().toString()
			entName = entName.replace(".java", "")
			if (it.isFile()) {
				def newClassName = entName + 'Repository'
				def newFileName = repositoryPath + entName + 'Repository.java'

				println newFileName
				def dst = new File( newFileName );
				dst.delete()
				dst.append('package com.cis.sys101.loader.repository;\n\n')
				dst.append('import java.util.UUID;\n')
				dst.append('import java.util.function.BiFunction;\n')
				dst.append('import org.springframework.data.repository.CrudRepository;\n')
				dst.append('import org.springframework.context.ApplicationContext;\n')
				dst.append('import org.springframework.stereotype.Repository;\n')
				dst.append('import com.cis.sys101.loader.config.ServiceConstants;\n')
				dst.append('import com.cis.sys101.loader.domain.' + entName + ';\n\n\n\n')
				dst.append('@Repository(value = ServiceConstants.' + entName.toString().toUpperCase() + '_DICTIONARY)\n')
				dst.append('public interface ' + newClassName + ' extends CrudRepository<' + entName + ', UUID> {}\n\n\n')
				//			dst.append('')
			}
		}
	}
	void createServiceConstants() {

		def dh = new File(domainPath)

		def dst = new File(serviceConstantsPath)

		dst.delete()

		dst.append('package com.cis.sys101.loader.config;\n')
		dst.append('import java.time.LocalDate;\n')
		dst.append('import java.util.HashMap;\n')
		dst.append('import java.util.Map;\n')
		dst.append('import java.util.UUID;\n')
		dst.append('import java.util.function.BiFunction;\n')
		dst.append('import java.util.function.Function;\n')
		dst.append('import java.util.ArrayList;\n')
		dst.append('import java.util.List;\n')
		dst.append('import java.lang.reflect.Method;\n')
		dst.append('import java.lang.reflect.InvocationTargetException;\n')

		dst.append('import org.slf4j.Logger;\n')
		dst.append('import org.slf4j.LoggerFactory;\n')


		dst.append('import org.springframework.context.ApplicationContext;\n')
		dh.eachFile {
			if (it.isFile()) {
				def domainName = it.getName().replace(".java", "")
				def repoName = domainName + 'Repository'
				dst.append('import com.cis.sys101.loader.domain.' + domainName + ';\n')
				dst.append('import com.cis.sys101.loader.repository.' + repoName  + ';\n')
			}
		}

		def allowableValues = ''

		dst.append('\n\npublic final class ServiceConstants {\n')
		dst.append('\tpublic static final String DB_SCHEME = "public";\n')
		dst.append('\tpublic static final String DB_DICTIONARY_SCHEME = "public";\n')
		dst.append('\tpublic static final LocalDate DATE = LocalDate.now();\n')
		dst.append('\tpublic static final String DICT_VERSION = "1";\n\n\n')

		dst.append('\tprivate static final Logger logger = LoggerFactory.getLogger(ServiceConstants.class);\n\n\n')

		dst.append('\tprivate static Map<String, ServiceClassWrapper> wrapper = new HashMap<>();\n')
		dst.append('\tprivate static Map<String, BiFunction<String, Object, Object>> saveMap = new HashMap<>();\n')
		dst.append('\tprivate static Map<String, Function<String, List<String>>> findAllMap = new HashMap<>();\n')

		dh.eachFile {
			if (it.isFile()) {


				def name = new String(it.getName().toString()).replace(".java", "")
				def nameUp = name.toUpperCase()
				def nameLo = name.toLowerCase()
				dst.append('\tpublic static final String ' + nameUp + '_DICTIONARY = "' + nameLo + '_dictionary";\n')
				allowableValues = allowableValues + ', ' +  nameLo + '_dictionary'
			}
		}

		dst.append('\n\n\tpublic static final String ALLOWABLEVALUES = "' + allowableValues + '";\n')
		dst.append('\n\n\tpublic static Map<String, ServiceClassWrapper> getWrapper() {\n\t\treturn wrapper;\n\t} \n')
		dst.append('\n\n\tpublic static Map<String, BiFunction<String, Object, Object>> getSaveMap() {\n\t\treturn saveMap;\n\t} \n')
		dst.append('\n\n\tpublic static Map<String, Function<String,List<String>>> getFindAllMap() {\n\t\t return findAllMap;\n\t } \n')
		dst.append('\n\n\tprivate static String getVersion() {\n\t\treturn UUID.randomUUID().toString().substring(0, 8);\n\t} \n')
		dst.append('\tpublic static void initServiceClassMap(ApplicationContext ctx) {\n')
		dh.eachFile {
			if (it.isFile()) {
				def runame = getDescription(it.toString())
				def entName = it.getName().toString().replace(".java", "")
				def repName = entName + 'Repository'
				if (runame != null) {
					def key = it.getName().replace(".java", "").toUpperCase()
					def value = 'ServiceClassWrapper.of("' + runame.trim() + '",' + entName + '.class, ' + repName + '.class)'


					dst.append('\t\tfindAllMap.put(' + key + '_DICTIONARY, (s) -> {\n')
					dst.append('\t\t\tList<String> res = new ArrayList<>();\n')
					dst.append('\t\t\tIterable<' + entName + '> lines = ctx.getBean(s, ' + repName + '.class).findAll();\n')
					dst.append('\t\t\tfor (' + entName + ' t : lines) {\n')
					dst.append('\t\t\t\tMethod[] methods = ' + entName + '.class.getDeclaredMethods();\n')
					dst.append('\t\t\t\tStringBuilder header = new StringBuilder();\n')
					dst.append('\t\t\t\tStringBuilder value = new StringBuilder();\n')
					dst.append('\t\t\t\tfor (Method m : methods) {\n')
					dst.append('\t\t\t\t\tif (m.getName().startsWith("get")) {\n')

					dst.append('\t\t\t\t\t\tif (res.size() == 0) {\n')
					dst.append('\t\t\t\t\t\t\theader.append(m.getName().replace("get", "").toLowerCase());\n')
					dst.append('\t\t\t\t\t\t\theader.append(",");\n')
					dst.append('\t\t\t\t\t\t}\n')




					dst.append('\t\t\t\t\t\ttry {\n')
					dst.append('\t\t\t\t\t\t\tvalue.append(m.invoke(t));\n')
					dst.append('\t\t\t\t\t\t\tvalue.append(",");\n')
					dst.append('\t\t\t\t\t\t} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {\n')
					dst.append('\t\t\t\t\t\t\t logger.error(e1.getMessage());\n')
					dst.append('\t\t\t\t\t\t}\n')

					dst.append('\t\t\t\t\t}\n')
					dst.append('\t\t\t\t}\n')

					dst.append('\t\t\t\tif (' + entName + '.class.getSuperclass() != null) {\n')
					dst.append('\t\t\t\t\tMethod[] superMethods = ' + entName + '.class.getSuperclass().getDeclaredMethods();\n')
					dst.append('\t\t\t\t\tfor (Method m : superMethods) {\n')
					dst.append('\t\t\t\t\t\tif (m.getName().startsWith("get") & m.getReturnType() != java.lang.Object.class) {\n')
					dst.append('\t\t\t\t\t\t\tif (res.size() == 0) {\n')
					dst.append('\t\t\t\t\t\t\t\theader.append(m.getName().replace("get", "").toLowerCase());\n')
					dst.append('\t\t\t\t\t\t\t\theader.append(",");\n')
					dst.append('\t\t\t\t\t\t}\n')


					dst.append('\t\t\t\t\t\ttry {\n')
					dst.append('\t\t\t\t\t\t\tvalue.append(m.invoke(t));\n')
					dst.append('\t\t\t\t\t\t\tvalue.append(",");\n')
					dst.append('\t\t\t\t\t\t} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {\n')
					dst.append('\t\t\t\t\t\t\t logger.error(e1.getMessage());\n')
					dst.append('\t\t\t\t\t\t}\n')
					dst.append('\t\t\t\t\t}\n')
					dst.append('\t\t\t\t}\n')
					dst.append('\t\t\t}\n')
					dst.append('\t\t\tif (res.size() == 0 && header.length() > 0) {\n ')
					dst.append('\t\t\t\tString s1 = header.toString().substring(0, header.toString().length()-1); \n')
					dst.append('\t\t\t\tres.add(s1);\n ')
					dst.append('\t\t\t}\n')

					dst.append('\t\t\tif (value.length() > 0) {\n ')
					dst.append('\t\t\t\tString s1 = value.toString().substring(0, value.toString().length()-1);\n ')
					dst.append('\t\t\t\tres.add(s1);\n ')
					dst.append('\t\t\t}\n')

					dst.append('\t\t}\n')
					dst.append('\t\treturn res;\n\t});\n\n ')

					dst.append('\t\tsaveMap.put('+ key+ '_DICTIONARY, (s, e) -> {\n' )
					dst.append('\t\t\t'+ entName + ' ne = (' + entName + ')e ;\n');
					dst.append('\t\t\tne.setVersion(getVersion());\n');

					if (it.getText().contains("AbstractStaticEntity")) {
						dst.append('\t\t\tif (ne.getId() == null) ne.setId(UUID.randomUUID());\n');
					}

					dst.append('\t\t\treturn ctx.getBean(s, ' + repName + '.class).save(ne);\n' )
					dst.append('\t\t});\n')
					dst.append('\t\twrapper.put(' + key + '_DICTIONARY,' + value + ');\n\n\n')
				}
			}
		}
		dst.append("\t}\n}\n");
	}


	static void listAll() {



		def dh = new File(domainPath)
		dh.eachFile {
			if (it.isFile()) {
				println(it.getName())
			}
		}
	}
	String getGreeting() {
		return 'Hello world.'
	}

	static void main(String[] args) {

		def app = new App()

		app.createServiceConstants()
		//app.createRepository()
	}
}
