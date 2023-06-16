package com.github.buckcri.web3auth

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan

@SpringBootApplication
@ServletComponentScan("io.github.buckcri.xclacks")
class Web3authApplication

fun main(args: Array<String>) {
	runApplication<Web3authApplication>(*args)
}
