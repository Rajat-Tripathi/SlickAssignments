package com.reactore.feature

/**
  * created by Kartik on 10-11-2017
  */

import com.reactore.core.HandleExceptions._
import com.reactore.core._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompanyService {
   self: CompanyFacadeComponent =>

   def insertCompany(company: Company): Future[Int] = {
      for {
         countryList <- countryRepository.countryFuture
         companyList <- companyRepository.companyFuture
         res = if (company.name.nonEmpty && company.licenceNumber.nonEmpty) {
            if (countryList.nonEmpty) {
               val validCountry = countryList.find(_.countryId == company.country)
               if (validCountry.isDefined) {
                  if (companyList.nonEmpty) {
                     val uniqueCountry = companyList.find(comp => comp.name.toLowerCase == company.name.toLowerCase && comp.licenceNumber.toLowerCase == company.licenceNumber.toLowerCase)
                     if (uniqueCountry.isEmpty) {
                        companyRepository.insert(company)
                     } else throw DuplicateEntityException(exception = new Exception("Company already defined!!"))
                  } else companyRepository.insert(company)
               } else throw NoSuchEntityException(exception = new Exception("Country does not exists!!"))
            } else throw EmptyListException(exception = new Exception("Country list is Empty!!"))
         } else throw FieldNotDefinedException(exception = new Exception("Fields are not defined!!"))
      } yield res
   }.flatten.recover { case ex => handleExceptions(ex) }

   //get company by id
   def getCompanyById(id: Long): Future[Company] = {
      for {
         companyList <- companyRepository.companyFuture
         res = if (companyList.nonEmpty) {
            val companyOption = companyList.find(_.companyId == id)
            if (companyOption.isDefined) {
               companyOption.get
            } else throw NoSuchEntityException(exception = new Exception("Company not found!!"))
         } else throw EmptyListException(exception = new Exception("Company list is empty!!"))
      } yield res
   }.recover { case ex => handleExceptions(ex) }

   //delete company by id
   def deleteCompanyById(id: Long): Future[Int] = {
      for {
         companyList <- companyRepository.companyFuture
         vehicleList <- vehicleRepository.vehiclesFuture
         res = if (companyList.nonEmpty) {
            val companyOption = companyList.find(_.companyId == id)
            if (companyOption.isDefined) {
               val vehiclesForGivenCompany = vehicleList.filter(_.company == id)
               if (vehiclesForGivenCompany.isEmpty) {
                  companyRepository.delete(id)
               } else throw ForeignKeyRelationFoundException(exception = new Exception("Foreign key relation found in vehicle table!!"))
            } else throw NoSuchEntityException(exception = new Exception("Company not found!!"))
         } else throw EmptyListException(exception = new Exception("Company list is empty!!"))
      } yield res
   }.flatten.recover { case ex => handleExceptions(ex) }

   //update company by id
   def updateCompanyById(id: Long, updatedCompany: Company): Unit = {
      for {
         companyList <- companyRepository.companyFuture
         countryList <- countryRepository.countryFuture
         res = if (updatedCompany.name.nonEmpty && updatedCompany.licenceNumber.nonEmpty) {
            val companyOption = companyList.find(_.companyId == id)
            if (companyOption.isDefined) {
               val validCountry = countryList.find(_.countryId == updatedCompany.country)
               if (validCountry.isDefined) {
                  val uniqueCompany = companyList.find(company => company.name.equalsIgnoreCase(updatedCompany.name) && company.licenceNumber.equalsIgnoreCase(updatedCompany.licenceNumber) && company.country == updatedCompany.country)
                  if (uniqueCompany.isEmpty) {
                     companyRepository.update(id, updatedCompany)
                  } else throw DuplicateEntityException(exception = new Exception("Updated company is already present!!"))
               } else throw NoSuchEntityException(exception = new Exception("No country found!!"))
            } else throw NoSuchEntityException(exception = new Exception("No company found for given id!!"))
         } else throw FieldNotDefinedException(exception = new Exception("Fields not defined!!"))
      } yield res

   }
}

object ImplCompanyService extends CompanyService with CompanyFacade