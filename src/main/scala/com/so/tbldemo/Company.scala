package com.so.tbldemo

/**
  * Created by a.reisberg on 9/3/2016.
  */
sealed trait Company

case class Department(name: String, employeeIds: List[String]) extends Company

case class Employee(name: String, age: Int, address: Address) extends Company

case class Address(city: String, zip: String)