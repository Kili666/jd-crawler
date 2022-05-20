package com.lqjai.crawler.config;

import com.gargoylesoftware.css.parser.CSSParseException;
import com.gargoylesoftware.htmlunit.DefaultCssErrorHandler;

/**
* Java爬虫httpunit配置
*@Author Kili
*@Date 2022/4/25 11:30
*/
public class HUnitCssErrorListener extends DefaultCssErrorHandler {
	@Override
	public void error(CSSParseException exception) {

	}

	@Override
	public void fatalError(CSSParseException exception) {

	}

	@Override
	public void warning(CSSParseException exception) {

	}
}