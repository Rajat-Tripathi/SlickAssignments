package com.reactore.feature

import com.reactore.core.HandleExceptions._
import com.reactore.core._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * created by Kartik on 11-11-2017
  */
class VehicleService {
   self: VehicleFacadeComponent =>

   //save vehicle
   def insertVehicle(vehicle: Vehicle): Future[Int] = {
      for {
         vehicleList <- vehicleRepository.vehiclesFuture
         vehicleTypeList <- vehicleTypeRepository.vehicleTypeFuture
         companyList <- companyRepository.companyFuture
         res = if (vehicle.name.nonEmpty && vehicle.modelNumber.nonEmpty) {
            if (companyList.nonEmpty) {
               val companyOption = companyList.find(_.companyId == vehicle.company)
               if (companyOption.isDefined) {
                  if (vehicleTypeList.nonEmpty) {
                     val vehicleTypeOption = vehicleTypeList.find(_.vehicleTypeId == vehicle.vehicleType)
                     if (vehicleTypeOption.isDefined) {
                        if (vehicleList.nonEmpty) {
                           val uniqueModelNumber = vehicleList.find(_.modelNumber.equalsIgnoreCase(vehicle.modelNumber))
                           if (uniqueModelNumber.isEmpty) {
                              vehicleRepository.insert(vehicle)
                           } else throw UniqueKeyViolationException(exception = new Exception("Unique model number violated!!"))
                        } else vehicleRepository.insert(vehicle)
                     } else throw NoSuchEntityException(exception = new Exception("Vehicle type not found!!"))
                  } else throw EmptyListException(exception = new Exception("Vehicle type list is empty!!"))
               } else throw NoSuchEntityException(exception = new Exception("Company not found!!"))
            } else throw EmptyListException(exception = new Exception("Company list is empty!!"))
         } else throw FieldNotDefinedException(exception = new Exception("Name/Model number is not defined!!"))
      } yield res
   }.flatten.recover { case ex => handleExceptions(ex) }

   //get vehicle by id
   def getVehicleById(id: Long): Future[Vehicle] = {
      for {
         vehicleList <- vehicleRepository.vehiclesFuture
         res = if (vehicleList.nonEmpty) {
            val vehicleOption = vehicleList.find(_.vehicleId == id)
            if (vehicleOption.isDefined) {
               vehicleOption.get
            } else throw NoSuchEntityException(exception = new Exception("Vehicle for not found given id !!"))
         } else throw EmptyListException(exception = new Exception("Vehicle list is empty!!"))
      } yield res
   }.recover { case ex => handleExceptions(ex) }

   //delete vehicle by id
   def deleteVehicleById(id: Long): Future[Int] = {
      for {
         vehicleList <- vehicleRepository.vehiclesFuture
         res = if (vehicleList.nonEmpty) {
            val vehicleOption = vehicleList.find(_.vehicleId == id)
            if (vehicleOption.isDefined) {
               vehicleRepository.delete(id)
            } else throw NoSuchEntityException(exception = new Exception("Vehicle for not found given id !!"))
         } else throw EmptyListException(exception = new Exception("Vehicle list is empty!!"))
      } yield res
   }.flatten.recover { case ex => handleExceptions(ex) }

   def updateVehicleById(id: Long, updatedVehicle: Vehicle): Future[Int] = {
      for {
         vehicleList <- vehicleRepository.vehiclesFuture
         vehicleTypeList <- vehicleTypeRepository.vehicleTypeFuture
         companyList <- companyRepository.companyFuture
         res = if (updatedVehicle.name.nonEmpty && updatedVehicle.modelNumber.nonEmpty) {
            val vehicleOption = vehicleList.find(vehicle => vehicle.vehicleId == id)
            if (vehicleOption.isDefined) {
               val validCompany = companyList.find(_.companyId == updatedVehicle.company)
               if (validCompany.isDefined) {
                  val validVehicleType = vehicleTypeList.find(_.vehicleTypeId == updatedVehicle.vehicleType)
                  if (validVehicleType.isDefined) {
                     val uniqueVehicle = vehicleList.find(_.modelNumber.equalsIgnoreCase(updatedVehicle.modelNumber))
                     if (uniqueVehicle.isEmpty) {
                        vehicleRepository.update(id,updatedVehicle)
                     } else throw UniqueKeyViolationException(exception = new Exception("Updated vehicle has duplicate code!!"))
                  } else throw NoSuchEntityException(exception = new Exception("Vehicle Type not found for updated data!!"))
               } else throw NoSuchEntityException(exception = new Exception("Company not found for updated data!!"))
            } else throw NoSuchEntityException(exception = new Exception("Vehicle not found for given id!!"))
         } else throw FieldNotDefinedException(exception = new Exception("All fields are not defined!!"))
      } yield res
   }.flatten.recover { case ex => handleExceptions(ex) }
}

class VehicleRest {

}