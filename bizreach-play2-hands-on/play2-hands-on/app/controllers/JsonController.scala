package controllers

import javax.inject.Inject

import models.Tables._
import play.api.db.slick._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile

import scala.concurrent.Future

object JsonController {
  // UsersRowをJSONに変換するためのWritesを定義
  implicit val usersRowWritesFormat = new Writes[UsersRow]{
    def writes(user: UsersRow): JsValue = {
      Json.obj(
        "id"        -> user.id,
        "name"      -> user.name,
        "companyId" -> user.companyId
      )
    }
  }

  // ユーザ情報を受け取るためのケースクラス
  case class UserForm(id: Option[Long], name: String, companyId: Option[Int])

  // JSONをUserFormに変換するためのReadsを定義
  implicit val userFormFormat = new Reads[UserForm]{
    override def reads(js: JsValue): JsResult[UserForm] = {
      JsSuccess(UserForm(
        id        = (js \ "id"       ).asOpt[Long],
        name      = (js \ "name"     ).as[String],
        companyId = (js \ "companyId").asOpt[Int]
      ))
    }
  }
}

class JsonController @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends Controller
  with HasDatabaseConfigProvider[JdbcProfile] {

  import JsonController._

  /**
    * 一覧表示
    */
  def list = Action.async { implicit rs =>
    // IDの昇順にすべてのユーザ情報を取得
    db.run(Users.sortBy(t => t.id).result).map { users =>
      // ユーザの一覧をJSONで返す
      Ok(Json.obj("users" -> users))
    }
  }

  /**
    * ユーザ登録
    */
  def create = Action.async(parse.json) { implicit rs =>
    rs.body.validate[UserForm].map { form =>
      // OKの場合はユーザを登録
      val user = UsersRow(0, form.name, form.companyId)
      db.run(Users += user).map { _ =>
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { e =>
      // NGの場合はバリデーションエラーを返す
      Future {
        BadRequest(Json.obj("result" ->"failure", "error" -> JsError.toJson(e)))
      }
    }
  }

  /**
    * ユーザ更新
    */
  def update = Action.async(parse.json) { implicit rs =>
    rs.body.validate[UserForm].map { form =>
      // OKの場合はユーザ情報を更新
      val user = UsersRow(form.id.get, form.name, form.companyId)
      db.run(Users.filter(t => t.id === user.id.bind).update(user)).map { _ =>
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { e =>
      // NGの場合はバリデーションエラーを返す
      Future {
        BadRequest(Json.obj("result" ->"failure", "error" -> JsError.toJson(e)))
      }
    }
  }

  /**
    * ユーザ削除
    */
  def remove(id: Long) = Action.async { implicit rs =>
    db.run(Users.filter(t => t.id === id.bind).delete).map { _ =>
      Ok(Json.obj("result" -> "success"))
    }
  }
}
