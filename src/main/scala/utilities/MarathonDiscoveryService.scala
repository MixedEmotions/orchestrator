package utilities

import org.slf4j.LoggerFactory

import scala.io.Source._
import scala.util.Random
import scala.util.parsing.json.JSON
/**
 * Created by cnavarro on 10/06/16.
 */
class MarathonDiscoveryService(dnsIp: String, dnsPort: Int) extends DiscoveryService {
  val logger = LoggerFactory.getLogger(MarathonDiscoveryService.this.getClass)

  def getIpAndPort(serviceId: String) : (String, Int) = {
    val mesosUrl = s"http://${this.dnsIp}:${this.dnsPort}/v1/services/_${serviceId}._tcp.marathon.mesos"
    //val response = utilities.MarathonServiceDiscovery.getURL(mesosUrl)
    logger.debug(s"Mesos Url: ${mesosUrl}")
    val response = MarathonDiscoveryService.getURL(mesosUrl)
    val mapList = JSON.parseFull(response).asInstanceOf[Some[List[Map[String, Any]]]]
    val addressesList = mapList.getOrElse(List(Map()).asInstanceOf[List[Map[String, Any]]])
    val length = addressesList.length
    if(length>0) {
      val random = Random
      val randomIndex = random.nextInt(length)
      logger.debug(s"Addresses length: ${length}, random: ${random}")
      logger.debug(s"AddressesList: ${addressesList}")
      val firstAddress = addressesList(randomIndex)
      logger.debug(s"Selected address: ${firstAddress}")
      val ip = firstAddress("ip").toString
      val port = firstAddress("port").toString.toInt
      (ip, port)
    }else{
      throw new Exception(s"Marathon Service ${mesosUrl} not found")
    }
  }
}

object MarathonDiscoveryService {
  /*def main(args: Array[String]) {
    val discovery = new MarathonDiscoveryService("localhost",8123)
    val (ip, port) = discovery.getIpAndPort("bridged-webapp")
    println(s"ip:${ip}, ${port}")

  }*/

  def getURL(url:String): String = {
    try {
      fromURL(url)("UTF-8").mkString
    }catch {
      case e: Exception => {
        fromURL(url)("ISO-8859-1").mkString
      }
    }
  }

  def fakeUrl(url: String): String = {
    """[
  {
   "service": "_bridged-webapp._tcp.marathon.mesos",
   "host": "bridged-webapp-r66a9-s0.marathon.slave.mesos.",
   "ip": "192.168.1.12",
   "port": "31585"
  },
  {
   "service": "_bridged-webapp._tcp.marathon.mesos",
   "host": "bridged-webapp-rqmyb-s0.marathon.slave.mesos.",
   "ip": "192.168.1.11",
   "port": "31510"
  }
 ]"""
  }

}
