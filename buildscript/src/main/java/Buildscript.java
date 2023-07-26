import io.github.coolcrabs.brachyura.added.basicbuildscript.*;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.fabric.FabricContext;
import io.github.coolcrabs.brachyura.fabric.FabricModule;
import io.github.coolcrabs.brachyura.ide.IdeModule;
import io.github.coolcrabs.brachyura.processing.sinks.DirectoryProcessingSink;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Util;
import sun.misc.Unsafe;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("unused")
public class Buildscript extends LegacyFabricProject {
	@SuppressWarnings("unused")
	public Buildscript() {
		super();

		SettingsCollector o = settings;

		String m = "https://maven.enginehub.org/repo/";
		o.dependency(
				m,
				"com.sk89q.worldedit",
				"worldedit-core",
				"6.1"//"7.2.9"
		);
		o.dependency(
				m,
				"com.sk89q",
				"jchronic",
				"0.2.4a"
		);
		o.dependency(
				m,
				"com.thoughtworks.paranamer",
				"paranamer",
				"2.6"
		);
		o.dependency(
				m,
				"com.sk89q.lib",
				"jlibnoise",
				"1.0.0"
		);

		o.src("src", "main", "java");
		o.resource("src", "main", "resources");
		o.template("src", "template", "java");

		o.templateMap("version", getVersion());
		o.templateMap("timestamp", getBuildDate("yyyy-MM-dd'T'HH:mm:ss'Z'"));
		o.templateMap("branch", GitHelper.BRANCH);
		o.templateMap("commit", GitHelper.COMMIT);
		o.templateMap("working_dir_clean", GitHelper.STATUS);
		o.templateMap("minecraft_version", DefaultVersions.MINECRAFT);
		//o.templateMap("yarn_mappings", DefaultVersions.LEGACY_YARN.mavenId.version);
		//o.templateMap("yarn_jar_url", DefaultVersions.LEGACY_YARN.asUrlString());

		String quilt_maven = "https://maven.quiltmc.org/repository/release";
		String ornithe_maven = "https://maven.ornithemc.net/releases";

		o.loader(quilt_maven, "org.quiltmc", "quilt-loader", "0.19.1");
		o.yarn(ornithe_maven, "net.ornithemc", "feather", "1.12.2+build.11");
		o.intermediary(ornithe_maven, "net.ornithemc", "calamus-intermediary", "1.12.2");
	}

	@Override
	public FabricModule createModule() {
		return new LegacyFabricModule(this, this.context.get(), this.settings) {
			@Override
			public Path[] getSrcDirs() {
				StackTraceElement[] trace = Thread.currentThread().getStackTrace();
				String caller = trace[2].getClassName() + "." + trace[2].getMethodName();
				Path[] x = getSrcDirs(caller).toArray(new Path[0]);
				System.out.println("return: " + Arrays.toString(x));
				return x;
			}

			private List<Path> getSrcDirs(String caller) {
				List<Path> srcDirs = (List<Path>) getField("src", project.settings);
				List<Path> templateDirs = (List<Path>) getField("templates", project.settings);

				switch (caller) {
					case "io.github.coolcrabs.brachyura.fabric.FabricModule.ideModule": {
						// for generating the ide module use this
						List<Path> paths = new ArrayList<>(srcDirs.size() + templateDirs.size());
						paths.addAll(srcDirs);
						paths.addAll(templateDirs);
						return paths;
					}
					case "io.github.coolcrabs.brachyura.fabric.FabricModule.createFabricCompilationResult": {
						// fill in the template, copy over, add that path to the build
						// TODO: do that

						List<Path> templatedTemplateDirs = (List<Path>) invoke(this, templateDirs);

						List<Path> paths = new ArrayList<>(srcDirs.size() + templateDirs.size());
						paths.addAll(srcDirs);
						paths.addAll(templatedTemplateDirs);
						return paths;
					}
					default: {
						System.out.println("[WARN]: getSrcDirs for caller isn't defined:");
						new Throwable().printStackTrace(System.out);

						List<Path> paths = new ArrayList<>(srcDirs.size() + templateDirs.size());
						paths.addAll(srcDirs);
						paths.addAll(templateDirs);
						return paths;
					}
				}
			}
		};
	}

	@Deprecated // replace with direct field access
	private static Object getField(String name, Object o) {
		try {
			Field f = SettingsCollector.class.getDeclaredField(name);
			f.setAccessible(true);
			return f.get(o);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	private static Object invoke(Object b, Object o) {
		try {
			Method m = LegacyFabricModule.class.getDeclaredMethod("copyTemplates", List.class);
			m.setAccessible(true);
			return m.invoke(b, o);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}


	@Override
	public void runRunConfig(IdeModule ideProject, IdeModule.RunConfig rc) {
		try {
			LinkedHashSet<IdeModule> toCompile = new LinkedHashSet<>();
			Deque<IdeModule> a = new ArrayDeque<>();
			a.add(ideProject);
			a.addAll(rc.additionalModulesClasspath);
			while (!a.isEmpty()) {
				IdeModule m = a.pop();
				if (!toCompile.contains(m)) {
					a.addAll(m.dependencyModules);
					toCompile.add(m);
				}
			}
			HashMap<IdeModule, Path> mmap = new HashMap<>();
			for (IdeModule m : toCompile) {
				JavaCompilation compilation = new JavaCompilation();
				compilation.addOption(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, m.javaVersion));
				compilation.addOption("-proc:none");
				for (JavaJarDependency dep : m.dependencies.get()) {
					compilation.addClasspath(dep.jar);
				}
				for (IdeModule m0 : m.dependencyModules) {
					compilation.addClasspath(Objects.requireNonNull(mmap.get(m0), "Bad module dep " + m0.name));
				}
				for (Path srcDir : m.sourcePaths) {
					compilation.addSourceDir(srcDir);
				}
				// ADDED
				List<Path> p = (List<Path>) getField("templates", settings);
				List<Path> p2 = (List<Path>) invoke(module.get(), p);
				for (Path template : p2) {
					compilation.addSourceDir(template);
				}
				// END ADDED
				Path outDir = Files.createTempDirectory("brachyurarun");
				JavaCompilationResult result = compilation.compile();
				Objects.requireNonNull(result);
				result.getInputs(new DirectoryProcessingSink(outDir));
				mmap.put(m, outDir);
			}
			ArrayList<String> command = new ArrayList<>();
			command.add(JvmUtil.CURRENT_JAVA_EXECUTABLE);
			command.addAll(rc.vmArgs.get());
			command.add("-cp");
			ArrayList<Path> cp = new ArrayList<>(rc.classpath.get());
			cp.addAll(ideProject.resourcePaths);
			cp.add(mmap.get(ideProject));
			for (IdeModule m : rc.additionalModulesClasspath) {
				cp.add(mmap.get(m));
			}
			StringBuilder cpStr = new StringBuilder();
			for (Path p : cp) {
				cpStr.append(p.toString());
				cpStr.append(File.pathSeparator);
			}
			cpStr.setLength(cpStr.length() - 1);
			command.add(cpStr.toString());
			command.add(rc.mainClass);
			command.addAll(rc.args.get());
			new ProcessBuilder(command)
					.inheritIO()
					.directory(rc.cwd.toFile())
					.start()
					.waitFor();
		} catch (Exception e) {
			throw Util.sneak(e);
		}
	}
}
