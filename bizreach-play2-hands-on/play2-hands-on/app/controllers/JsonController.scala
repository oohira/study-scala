package controllers

import javax.inject.Inject

import play.api.db.slick._
import play.api.mvc._
import slick.driver.JdbcProfile

class JsonController @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends Controller
  with HasDatabaseConfigProvider[JdbcProfile] {

  /**
    * 一覧表示
    */
  def list = TODO

  /**
    * ユーザ登録
    */
  def create = TODO

  /**
    * ユーザ更新
    */
  def update = TODO

  /**
    * ユーザ削除
    */
  def remove(id: Long) = TODO
}
