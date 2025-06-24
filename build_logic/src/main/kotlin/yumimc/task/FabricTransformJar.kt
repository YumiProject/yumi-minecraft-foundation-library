package yumimc.task

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.jvm.tasks.Jar
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import javax.inject.Inject

abstract class FabricTransformJar @Inject constructor() : Jar() {
	@InputFile
	val inputJar: RegularFileProperty = project.objects.fileProperty()

	override fun copy() {
		super.copy()

		val inputJar = this.inputJar.asFile.get().toPath()
		val outputJar = this.archiveFile.get().asFile.toPath()

		FileSystems.newFileSystem(outputJar).use { outFs ->
			this.copyJar(inputJar, outFs)
		}
	}

	private fun copyJar(inputJar: Path, outFs: FileSystem) {
		FileSystems.newFileSystem(inputJar).use { inFs ->
			val excludeFiles = listOf(
				inFs.getPath("/META-INF/neoforge.mods.toml"),
			)

			inFs.rootDirectories.forEach { root ->
				Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
					override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
						Files.createDirectories(outFs.getPath("$dir"))

						return FileVisitResult.CONTINUE
					}

					override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
						if (file in excludeFiles) {
							return FileVisitResult.CONTINUE
						}

						Files.copy(file, outFs.getPath("$file"), StandardCopyOption.REPLACE_EXISTING)

						return FileVisitResult.CONTINUE
					}
				})
			}
		}
	}
}
