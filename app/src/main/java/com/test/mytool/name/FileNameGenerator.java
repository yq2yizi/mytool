package com.test.mytool.name;

/**
 * Generates names for files at disk cache
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.3.1
 */
public interface FileNameGenerator {

	/** Generates unique file name for image defined by URI */
	String generate(String imageUri);
}
